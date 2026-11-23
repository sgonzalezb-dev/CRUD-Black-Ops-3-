package com.bo3zombies.arsenal.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/** Barra de desplazamiento fina: sin flechas, con "thumb" redondeado que se ilumina en cian. */
public class ScrollBarOscura extends BasicScrollBarUI {

    private boolean hover;

    /** Aplica el estilo oscuro a ambas barras de un JScrollPane. */
    public static void aplicar(JScrollPane scroll, Color fondo) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.setBackground(fondo);
        scroll.getViewport().setBackground(fondo);
        for (JScrollBar barra : new JScrollBar[]{scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar()}) {
            if (barra != null) {
                barra.setUI(new ScrollBarOscura());
                barra.setBackground(fondo);
                barra.setUnitIncrement(16);
            }
        }
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        scrollBarWidth = 10;
        minimumThumbSize = new Dimension(10, 36);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        scrollbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                scrollbar.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                scrollbar.repaint();
            }
        });
    }

    @Override
    protected JButton createDecreaseButton(int orientacion) {
        return botonInvisible();
    }

    @Override
    protected JButton createIncreaseButton(int orientacion) {
        return botonInvisible();
    }

    private static JButton botonInvisible() {
        JButton boton = new JButton();
        Dimension cero = new Dimension(0, 0);
        boton.setPreferredSize(cero);
        boton.setMinimumSize(cero);
        boton.setMaximumSize(cero);
        boton.setFocusable(false);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorder(BorderFactory.createEmptyBorder());
        return boton;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g2 = Tema.suavizar(g);
        Color color;
        if (isDragging) {
            color = Tema.NEON;
        } else if (hover) {
            color = Tema.mezclar(Tema.BORDE, Tema.NEON, 0.55);
        } else {
            color = Tema.mezclar(Tema.BORDE, Tema.TEXTO_APAGADO, 0.6);
        }
        g2.setColor(color);
        int pad = 2;
        boolean vertical = scrollbar.getOrientation() == Adjustable.VERTICAL;
        int grosor = (vertical ? r.width : r.height) - pad * 2;
        g2.fillRoundRect(r.x + pad, r.y + pad, r.width - pad * 2, r.height - pad * 2, grosor, grosor);
        g2.dispose();
    }
}
