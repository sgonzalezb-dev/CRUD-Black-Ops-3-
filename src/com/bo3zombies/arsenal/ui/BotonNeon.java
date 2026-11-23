package com.bo3zombies.arsenal.ui;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Botón redondeado suavizado con degradado sutil, resplandor cálido en hover y estado deshabilitado. */
public class BotonNeon extends JButton {

    private final Color acento;
    private final boolean primario;
    private boolean hover;
    private boolean presionado;
    private float brillo;
    private final Timer animacion;

    public BotonNeon(String texto, Color acento) {
        this(texto, acento, false);
    }

    /** @param primario si es true el botón se pinta relleno con degradado del color de acento. */
    public BotonNeon(String texto, Color acento, boolean primario) {
        super(texto);
        this.acento = acento;
        this.primario = primario;
        setFont(Tema.BOTON);
        setForeground(acento);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setMargin(new Insets(0, 0, 0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animacion = new Timer(15, e -> {
            float objetivo = hover && isEnabled() ? 1f : 0f;
            brillo += (objetivo - brillo) * 0.20f;
            if (Math.abs(objetivo - brillo) < 0.02f) {
                brillo = objetivo;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                animacion.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                presionado = false;
                animacion.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    presionado = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                presionado = false;
                repaint();
            }
        });
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
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        setCursor(Cursor.getPredefinedCursor(habilitado ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (!habilitado) {
            brillo = 0f;
            presionado = false;
        } else if (hover && animacion != null) {
            animacion.start();
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 44, 42);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        float m = 3f;
        float r = Tema.RADIO_CONTROL;
        RoundRectangle2D forma = new RoundRectangle2D.Float(m, m, w - 2 * m - 1, h - 2 * m - 1, r, r);
        boolean activo = isEnabled();
        Color c = activo ? acento : Tema.TEXTO_APAGADO;

        // Resplandor exterior suave al pasar el ratón
        if (activo && brillo > 0f) {
            for (int i = 3; i >= 1; i--) {
                g2.setColor(Tema.alfa(c, Math.round(18 * brillo)));
                g2.setStroke(new BasicStroke(i * 2.4f));
                g2.draw(forma);
            }
        }

        // Relleno suavizado
        if (primario && activo) {
            Color arriba = presionado
                    ? Tema.mezclar(c, Color.BLACK, 0.16)
                    : Tema.mezclar(c, Color.WHITE, 0.14 + 0.10 * brillo);
            Color abajo = presionado
                    ? Tema.mezclar(c, Color.BLACK, 0.28)
                    : Tema.mezclar(c, Color.BLACK, 0.10 - 0.05 * brillo);
            g2.setPaint(new GradientPaint(0, m, arriba, 0, h - m, abajo));
        } else {
            int a = presionado ? 75 : Math.round((activo ? 18 : 8) + 38 * brillo);
            g2.setColor(Tema.alfa(c, a));
        }
        g2.fill(forma);

        // Borde sutil
        g2.setStroke(new BasicStroke(isFocusOwner() && activo ? 1.8f : 1.15f));
        if (!activo) {
            g2.setColor(Tema.alfa(c, 80));
        } else if (primario) {
            g2.setColor(Tema.alfa(Tema.PALETA_BLANCO, 65));
        } else {
            g2.setColor(Tema.alfa(c, Math.round(130 + 100 * brillo)));
        }
        g2.draw(forma);

        // Texto
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String texto = getText();
        int x = (w - fm.stringWidth(texto)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent() + (presionado ? 1 : 0);
        if (!activo) {
            g2.setColor(Tema.TEXTO_APAGADO);
        } else if (primario) {
            g2.setColor(Tema.PALETA_NEGRO);
        } else {
            g2.setColor(Tema.mezclar(c, Tema.PALETA_BLANCO, 0.45 + 0.35 * brillo));
        }
        g2.drawString(texto, x, y);
        g2.dispose();
    }
}
