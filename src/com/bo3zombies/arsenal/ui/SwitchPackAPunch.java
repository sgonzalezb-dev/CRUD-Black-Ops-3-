package com.bo3zombies.arsenal.ui;

import javax.swing.JCheckBox;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/** Interruptor animado estilo neón para el estado "Pack-a-Punch". */
public class SwitchPackAPunch extends JCheckBox {

    private float progreso;
    private boolean hover;
    private final Timer animacion;

    public SwitchPackAPunch(String texto) {
        super(texto);
        setOpaque(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(Tema.CUERPO_NEGRITA);
        setForeground(Tema.TEXTO);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animacion = new Timer(15, e -> {
            float objetivo = isSelected() ? 1f : 0f;
            progreso += (objetivo - progreso) * 0.28f;
            if (Math.abs(objetivo - progreso) < 0.02f) {
                progreso = objetivo;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addItemListener(e -> animacion.start());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
        if (!isShowing()) {
            progreso = b ? 1f : 0f;
        } else if (animacion != null) {
            animacion.start();
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(260, 42);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();

        // Tarjeta contenedora sutil
        int r = Tema.RADIO_CONTROL;
        Color bordeFondo = isSelected()
                ? Tema.mezclar(Tema.BORDE, Tema.NEON, 0.55)
                : hover ? Tema.mezclar(Tema.BORDE, Tema.TEXTO_TENUE, 0.35) : Tema.BORDE;
        Color rellenoFondo = isSelected()
                ? Tema.mezclar(Tema.PANEL_ALT, Tema.NEON, 0.11)
                : Tema.PANEL_ALT;
        g2.setColor(rellenoFondo);
        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
        g2.setColor(bordeFondo);
        g2.setStroke(new BasicStroke(1.1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

        // Etiqueta izquierda
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(isSelected() ? Tema.NEON : Tema.TEXTO);
        g2.drawString(getText(), 12, textY);

        // Estado textual breve
        String estado = isSelected() ? "ACTIVO" : "NO";
        g2.setFont(Tema.ETIQUETA_MINI);
        FontMetrics fmEst = g2.getFontMetrics();
        int trackW = 44;
        int trackH = 22;
        int trackX = w - trackW - 12;
        int trackY = (h - trackH) / 2;
        g2.setColor(isSelected() ? Tema.NEON : Tema.TEXTO_APAGADO);
        g2.drawString(estado, trackX - fmEst.stringWidth(estado) - 10,
                (h - fmEst.getHeight()) / 2 + fmEst.getAscent());

        // Pista del interruptor
        RoundRectangle2D pista = new RoundRectangle2D.Float(trackX, trackY, trackW, trackH, trackH, trackH);
        if (progreso > 0.05f) {
            g2.setColor(Tema.alfa(Tema.NEON, Math.round(45 * progreso)));
            g2.setStroke(new BasicStroke(4f));
            g2.draw(pista);
        }
        g2.setColor(Tema.mezclar(Tema.PANEL_CABECERA, Tema.NEON, progreso * 0.85));
        g2.fill(pista);
        g2.setColor(Tema.mezclar(Tema.BORDE, Tema.NEON, progreso));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(pista);

        // Botón deslizante
        float knob = trackH - 6;
        float knobX = trackX + 3 + (trackW - knob - 6) * progreso;
        float knobY = trackY + 3;
        g2.setColor(Tema.mezclar(Tema.TEXTO_TENUE, Color.WHITE, progreso));
        g2.fill(new Ellipse2D.Float(knobX, knobY, knob, knob));
        g2.dispose();
    }
}
