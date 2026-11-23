package com.bo3zombies.arsenal.service;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;
import com.bo3zombies.arsenal.persistence.ArsenalRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Lógica CRUD del arsenal.
 * <p>
 * Mantiene la colección en memoria y la persiste en disco después de CADA
 * operación de escritura. Si el guardado falla, el cambio se revierte en
 * memoria para que la pantalla y el archivo nunca se desincronicen.
 */
public class ArsenalService {

    private final ArsenalRepository repositorio;
    private final List<ItemArsenal> items;
    private final List<Consumer<EstadoGuardado>> oyentes = new ArrayList<>();
    private LocalDateTime ultimaSincronizacion;

    public ArsenalService(ArsenalRepository repositorio, List<ItemArsenal> iniciales) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.items = new ArrayList<>(Objects.requireNonNull(iniciales));
    }

    // ───────────────────────────── LEER ─────────────────────────────

    /** Copia de solo lectura: nadie de fuera puede modificar la colección interna. */
    public List<ItemArsenal> listarTodos() {
        return List.copyOf(items);
    }

    public Optional<ItemArsenal> buscarPorId(int id) {
        return items.stream().filter(i -> i.id() == id).findFirst();
    }

    public int cantidad() {
        return items.size();
    }

    public int siguienteId() {
        return items.stream().mapToInt(ItemArsenal::id).max().orElse(0) + 1;
    }

    public Map<Categoria, Long> contarPorCategoria() {
        Map<Categoria, Long> conteo = new EnumMap<>(Categoria.class);
        for (Categoria c : Categoria.values()) {
            conteo.put(c, 0L);
        }
        for (ItemArsenal item : items) {
            conteo.merge(item.categoria(), 1L, Long::sum);
        }
        return conteo;
    }

    // ───────────────────────────── CREAR ─────────────────────────────

    public ItemArsenal crear(String nombre, Categoria categoria, String mapaOrigen,
                             String rarezaONivel, boolean packAPunch, String descripcion) {
        ItemArsenal nuevo = new ItemArsenal(siguienteId(), nombre, categoria, mapaOrigen,
                rarezaONivel, packAPunch, descripcion);
        verificarDuplicado(nuevo);

        items.add(nuevo);
        try {
            persistir();
        } catch (PersistenciaException e) {
            items.remove(items.size() - 1); // rollback
            throw e;
        }
        return nuevo;
    }

    // ─────────────────────────── ACTUALIZAR ───────────────────────────

    /** Crea un record NUEVO con los valores editados y reemplaza al anterior (mismo id). */
    public ItemArsenal actualizar(int id, String nombre, Categoria categoria, String mapaOrigen,
                                  String rarezaONivel, boolean packAPunch, String descripcion) {
        int indice = indiceDe(id);
        ItemArsenal actualizado = new ItemArsenal(id, nombre, categoria, mapaOrigen,
                rarezaONivel, packAPunch, descripcion);
        verificarDuplicado(actualizado);

        ItemArsenal anterior = items.set(indice, actualizado);
        try {
            persistir();
        } catch (PersistenciaException e) {
            items.set(indice, anterior); // rollback
            throw e;
        }
        return actualizado;
    }

    // ───────────────────────────── BORRAR ─────────────────────────────

    public ItemArsenal eliminar(int id) {
        int indice = indiceDe(id);
        ItemArsenal eliminado = items.remove(indice);
        try {
            persistir();
        } catch (PersistenciaException e) {
            items.add(indice, eliminado); // rollback en la misma posición
            throw e;
        }
        return eliminado;
    }

    // ─────────────────────────── AUXILIARES ───────────────────────────

    private int indiceDe(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id() == id) {
                return i;
            }
        }
        throw new IllegalArgumentException("El registro #" + id + " ya no existe.");
    }

    private void verificarDuplicado(ItemArsenal candidato) {
        boolean duplicado = items.stream().anyMatch(i ->
                i.id() != candidato.id()
                        && i.categoria() == candidato.categoria()
                        && i.nombre().equalsIgnoreCase(candidato.nombre()));
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe «" + candidato.nombre()
                    + "» en la categoría " + candidato.categoria().getEtiqueta() + ".");
        }
    }

    private void persistir() {
        notificar(EstadoGuardado.GUARDANDO);
        try {
            repositorio.guardar(items);
            ultimaSincronizacion = LocalDateTime.now();
            notificar(EstadoGuardado.SINCRONIZADO);
        } catch (IOException e) {
            notificar(EstadoGuardado.ERROR);
            throw new PersistenciaException(
                    "No se pudo escribir en " + repositorio.getArchivo() + ": " + e.getMessage(), e);
        }
    }

    // ───────────────────── Estado de sincronización ─────────────────────

    public void agregarOyente(Consumer<EstadoGuardado> oyente) {
        oyentes.add(Objects.requireNonNull(oyente));
    }

    private void notificar(EstadoGuardado estado) {
        for (Consumer<EstadoGuardado> oyente : oyentes) {
            oyente.accept(estado);
        }
    }

    public LocalDateTime getUltimaSincronizacion() {
        return ultimaSincronizacion;
    }

    public Path getRutaArchivo() {
        return repositorio.getArchivo();
    }

    public boolean existeArchivo() {
        return repositorio.existeArchivo();
    }
}
