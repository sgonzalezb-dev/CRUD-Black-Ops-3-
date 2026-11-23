package com.bo3zombies.arsenal.ui;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Pequeño punto de color con halo (para categorías y mensajes). */
public class IconoPunto implements Icon {

    private final Color color;
    private final int tamano;

    public IconoPunto(Color color) {
        this(color, 10);
    }

    public IconoPunto(Color color, int tamano) {
        this.color = color;
        this.tamano = tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Tema.suavizar(g);
        g2.setColor(Tema.alfa(color, 60));
        g2.fill(new Ellipse2D.Float(x, y, tamano, tamano));
        float interior = tamano * 0.6f;
        float off = (tamano - interior) / 2f;
        g2.setColor(color);
        g2.fill(new Ellipse2D.Float(x + off, y + off, interior, interior));
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return tamano;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }
}
