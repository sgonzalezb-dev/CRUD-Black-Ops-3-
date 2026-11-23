package com.bo3zombies.arsenal.service;

import java.io.Serial;

/** Se lanza cuando un cambio no pudo guardarse en disco (y por tanto se revirtió). */
public class PersistenciaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
