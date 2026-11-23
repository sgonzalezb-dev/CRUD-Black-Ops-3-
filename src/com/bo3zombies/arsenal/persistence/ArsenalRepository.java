package com.bo3zombies.arsenal.persistence;

import com.bo3zombies.arsenal.model.ItemArsenal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia del arsenal en un archivo binario mediante serialización nativa
 * de Java ({@link ObjectOutputStream} / {@link ObjectInputStream}).
 * <p>
 * El guardado es <b>atómico y forzado a disco</b>: se escribe primero en un
 * archivo temporal, se sincroniza con el disco físico ({@code getFD().sync()})
 * y luego se reemplaza el archivo real. Así, aunque se vaya la luz a mitad de
 * un guardado, en disco siempre queda la versión anterior completa o la nueva
 * completa, nunca un archivo a medias.
 */
public class ArsenalRepository {

    public static final String NOMBRE_ARCHIVO = "bo3_zombies_arsenal.dat";

    /** Propiedad opcional para usar otra ruta: java -Dbo3.archivo=ruta\archivo.dat ... */
    public static final String PROPIEDAD_RUTA = "bo3.archivo";

    /** Solo se permite deserializar las clases que realmente usamos (defensa ante archivos maliciosos). */
    private static final String FILTRO_CLASES =
            "maxdepth=8;maxrefs=200000;"
                    + "com.bo3zombies.arsenal.model.*;java.util.ArrayList;java.lang.Enum;java.lang.Object;!*";

    private final Path archivo;

    public ArsenalRepository() {
        this(rutaPorDefecto());
    }

    public ArsenalRepository(Path archivo) {
        this.archivo = archivo.toAbsolutePath().normalize();
    }

    private static Path rutaPorDefecto() {
        String personalizada = System.getProperty(PROPIEDAD_RUTA);
        if (personalizada != null && !personalizada.isBlank()) {
            return Path.of(personalizada);
        }
        return Path.of("data", NOMBRE_ARCHIVO);
    }

    public Path getArchivo() {
        return archivo;
    }

    public boolean existeArchivo() {
        return Files.exists(archivo);
    }

    /**
     * Lee el arsenal desde disco. Si el archivo aún no existe (primera
     * ejecución) devuelve una lista vacía sin lanzar errores.
     */
    public List<ItemArsenal> cargar() throws IOException {
        if (Files.notExists(archivo)) {
            return new ArrayList<>();
        }
        if (Files.size(archivo) == 0) {
            throw new IOException("el archivo está vacío");
        }
        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(FILTRO_CLASES));
            Object leido = entrada.readObject();

            if (!(leido instanceof List<?> lista)) {
                throw new IOException("formato inesperado en el archivo");
            }
            List<ItemArsenal> items = new ArrayList<>(lista.size());
            for (Object elemento : lista) {
                if (!(elemento instanceof ItemArsenal item)) {
                    throw new IOException("el archivo contiene un elemento inválido");
                }
                items.add(item);
            }
            return items;
        } catch (ClassNotFoundException | ClassCastException | IllegalArgumentException e) {
            throw new IOException("archivo incompatible o dañado (" + e.getMessage() + ")", e);
        }
    }

    /** Guarda la lista completa de forma atómica y forzando la escritura física en disco. */
    public synchronized void guardar(List<ItemArsenal> items) throws IOException {
        Path carpeta = archivo.getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try {
            try (FileOutputStream fos = new FileOutputStream(temporal.toFile());
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 ObjectOutputStream salida = new ObjectOutputStream(bos)) {

                salida.writeObject(new ArrayList<>(items)); // ArrayList es Serializable
                salida.flush();                             // vacía los buffers hacia el FileOutputStream
                fos.getFD().sync();                         // obliga al SO a escribir en el disco físico
            }
            reemplazarConReintentos(temporal);
        } finally {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignorada) {
                // Si no se puede borrar el temporal no es grave: se sobrescribe en el próximo guardado.
            }
        }
    }

    /**
     * Mueve el temporal sobre el archivo real. Reintenta unas cuantas veces
     * porque en Windows (y en carpetas de OneDrive) el archivo puede estar
     * bloqueado un instante por el antivirus o el sincronizador.
     */
    private void reemplazarConReintentos(Path temporal) throws IOException {
        FileSystemException ultimoError = null;
        for (int intento = 1; intento <= 6; intento++) {
            try {
                try {
                    Files.move(temporal, archivo,
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            } catch (FileSystemException e) {
                ultimoError = e;
                esperar(70L * intento);
            }
        }
        throw ultimoError;
    }

    /** Renombra un archivo ilegible para no perderlo y permitir arrancar con una lista vacía. */
    public Path respaldarArchivoDanado() throws IOException {
        String sello = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path destino = archivo.resolveSibling(archivo.getFileName() + ".corrupto-" + sello);
        Files.move(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
        return destino;
    }

    private static void esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
