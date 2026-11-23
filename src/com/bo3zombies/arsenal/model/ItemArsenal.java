package com.bo3zombies.arsenal.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Una pieza del arsenal de BO3 Zombies.
 * <p>
 * Es un {@code record}: inmutable por diseño. Para "actualizar" un registro se
 * crea un record nuevo con el mismo id y se reemplaza en la colección.
 * <p>
 * Importante: al deserializar un record, Java invoca su constructor canónico,
 * así que las validaciones del constructor compacto también protegen contra
 * archivos dañados o manipulados.
 */
public record ItemArsenal(
        int id,
        String nombre,
        Categoria categoria,
        String mapaOrigen,
        String rarezaONivel,
        boolean packAPunch,
        String descripcion
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int MAX_NOMBRE = 60;
    public static final int MAX_TEXTO_CORTO = 40;
    public static final int MAX_DESCRIPCION = 500;

    /** Constructor compacto: valida y normaliza todos los campos. */
    public ItemArsenal {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }
        nombre = requerido(nombre, "nombre", MAX_NOMBRE);
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        mapaOrigen = requerido(mapaOrigen, "mapa de origen", MAX_TEXTO_CORTO);
        rarezaONivel = requerido(rarezaONivel, "rareza o nivel", MAX_TEXTO_CORTO);
        descripcion = descripcion == null ? "" : descripcion.strip();
        if (descripcion.length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar " + MAX_DESCRIPCION + " caracteres.");
        }
    }

    private static String requerido(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.strip();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " no puede superar " + maximo + " caracteres.");
        }
        return limpio;
    }

    /** Devuelve una copia con otro id (los records no tienen setters). */
    public ItemArsenal conId(int nuevoId) {
        return new ItemArsenal(nuevoId, nombre, categoria, mapaOrigen, rarezaONivel, packAPunch, descripcion);
    }
}
