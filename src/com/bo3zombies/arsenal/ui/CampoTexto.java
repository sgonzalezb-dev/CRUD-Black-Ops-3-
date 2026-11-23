package com.bo3zombies.arsenal.ui;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/** Campo de texto oscuro con esquinas redondeadas, borde neón al enfocar, placeholder y estado de error. */
public class CampoTexto extends JTextField {

    private final String placeholder;
    private final boolean busqueda;
    private boolean error;

    public CampoTexto(String placeholder) {
        this(placeholder, false);
    }

    /** @param busqueda si es true se dibuja un icono de lupa a la izquierda. */
    public CampoTexto(String placeholder, boolean busqueda) {
        this.placeholder = placeholder;
        this.busqueda = busqueda;
        setOpaque(false);
        setFont(Tema.CUERPO);
        setForeground(Tema.TEXTO);
        setCaretColor(Tema.NEON);
        setSelectionColor(Tema.SELECCION_TEXTO);
        setSelectedTextColor(Color.WHITE);
        setDisabledTextColor(Tema.TEXTO_APAGADO);
        setBorder(new EmptyBorder(9, busqueda ? 38 : 12, 9, 12));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        Tema.alCambiar(getDocument(), () -> {
            if (error) {
                setError(false);
            }
        });
    }

    public void setLimite(int maximo) {
        ((AbstractDocument) getDocument()).setDocumentFilter(new LimiteDocumento(maximo));
    }

    public void setError(boolean error) {
        this.error = error;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        int r = Tema.RADIO_CONTROL;

        Graphics2D fondo = Tema.suavizar(g);
        fondo.setColor(isEnabled() ? Tema.PANEL_ALT : Tema.PANEL);
        fondo.fillRoundRect(0, 0, w - 1, h - 1, r, r);
        fondo.dispose();

        super.paintComponent(g);

        Graphics2D g2 = Tema.suavizar(g);
        boolean foco = isFocusOwner();

        if (getText().isEmpty() && placeholder != null) {
            g2.setFont(getFont());
            g2.setColor(Tema.TEXTO_APAGADO);
            FontMetrics fm = g2.getFontMetrics();
            Insets in = getInsets();
            int base = getBaseline(w, h);
            int y = base >= 0 ? base : (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, in.left, y);
        }

        if (busqueda) {
            g2.setColor(foco ? Tema.NEON : Tema.TEXTO_TENUE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float cx = 14f;
            float cy = h / 2f - 2f;
            g2.draw(new Ellipse2D.Float(cx, cy - 5f, 10f, 10f));
            g2.draw(new Line2D.Float(cx + 8.6f, cy + 3.6f, cx + 12.5f, cy + 7.5f));
        }

        Color borde = error ? Tema.SANGRE : foco ? Tema.NEON : Tema.BORDE;
        if (foco || error) {
            g2.setColor(Tema.alfa(borde, 38));
            g2.setStroke(new BasicStroke(3.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
        }
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(1.1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
        g2.dispose();
    }
}
