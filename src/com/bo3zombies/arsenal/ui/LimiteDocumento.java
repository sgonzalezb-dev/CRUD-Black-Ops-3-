package com.bo3zombies.arsenal.ui;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Limita la cantidad de caracteres que se pueden escribir en un campo de texto. */
final class LimiteDocumento extends DocumentFilter {

    private final int maximo;

    LimiteDocumento(int maximo) {
        this.maximo = maximo;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
            throws BadLocationException {
        replace(fb, offset, 0, texto, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int longitud, String texto, AttributeSet attrs)
            throws BadLocationException {
        String nuevo = texto == null ? "" : texto;
        int disponible = maximo - (fb.getDocument().getLength() - longitud);
        if (disponible <= 0 && !nuevo.isEmpty()) {
            Toolkit.getDefaultToolkit().beep();
            if (longitud > 0) {
                super.replace(fb, offset, longitud, "", attrs);
            }
            return;
        }
        if (nuevo.length() > disponible) {
            nuevo = nuevo.substring(0, Math.max(0, disponible));
            Toolkit.getDefaultToolkit().beep();
        }
        super.replace(fb, offset, longitud, nuevo, attrs);
    }
}
