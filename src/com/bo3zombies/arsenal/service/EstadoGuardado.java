package com.bo3zombies.arsenal.service;

/** Estado de sincronización entre la memoria y el archivo local. */
public enum EstadoGuardado {
    /** Todavía no existe el archivo: se creará con el primer registro. */
    SIN_ARCHIVO,
    /** Escribiendo en disco. */
    GUARDANDO,
    /** Memoria y archivo coinciden. */
    SINCRONIZADO,
    /** El último guardado falló (el cambio se revirtió). */
    ERROR
}
