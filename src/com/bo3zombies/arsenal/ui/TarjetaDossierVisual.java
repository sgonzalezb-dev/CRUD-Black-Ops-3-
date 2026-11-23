package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Tarjeta panorámica de exhibición visual ("Dossier Táctico") que muestra el
 * arte del juego correspondiente al equipamiento seleccionado o a la categoría
 * activa en el formulario, con degradados suavizados y ficha resumida.
 */
public class TarjetaDossierVisual extends JPanel {

    private ItemArsenal itemActual;
    private Categoria categoriaPreview = Categoria.ARMA_MARAVILLA;

    public TarjetaDossierVisual() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 124));
        setMinimumSize(new Dimension(0, 114));
    }

    public void mostrarItem(ItemArsenal item) {
        this.itemActual = item;
        if (item != null) {
            this.categoriaPreview = item.categoria();
        }
        repaint();
    }

    public void mostrarPreviewCategoria(Categoria categoria) {
        this.itemActual = null;
        this.categoriaPreview = categoria != null ? categoria : Categoria.ARMA_MARAVILLA;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        int r = Tema.RADIO_TARJETA;

        Categoria cat = itemActual != null ? itemActual.categoria() : categoriaPreview;
        Color acento = Tema.colorCategoria(cat);
        BufferedImage arte = Recursos.arteCategoria(cat);

        // Fondo oscuro base redondeado
        RoundRectangle2D clip = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r);
        g2.setColor(Tema.PANEL_CABECERA);
        g2.fill(clip);

        // Arte del juego ocupando el lado derecho / panorámico con fundido suave hacia la izquierda
        if (arte != null) {
            int imgW = Math.max(260, (int) (w * 0.52));
            int imgX = w - imgW;
            Recursos.pintarCover(g2, arte, imgX, 0, imgW, h, r, 0.92f);

            // Fundido horizontal suave desde el panel izquierdo hacia el arte
            Graphics2D grad = (Graphics2D) g2.create();
            grad.clip(clip);
            grad.setPaint(new GradientPaint(
                    imgX - 10, 0, Tema.PANEL_CABECERA,
                    imgX + imgW * 0.58f, 0, Tema.alfa(Tema.PANEL_CABECERA, 25)));
            grad.fillRect(imgX - 20, 0, imgW + 20, h);
            // Suave viñeta inferior
            grad.setPaint(new GradientPaint(0, h * 0.55f, Tema.alfa(Tema.PALETA_NEGRO, 0),
                    0, h, Tema.alfa(Tema.PALETA_NEGRO, 125)));
            grad.fillRect(0, 0, w, h);
            grad.dispose();
        }

        // Barra vertical de acento suavizada a la izquierda
        g2.setColor(acento);
        g2.fillRoundRect(14, 18, 3, h - 36, 3, 3);

        // Contenido textual izquierdo
        int tx = 28;
        int maxTextW = Math.max(200, (int) (w * 0.58));

        if (itemActual != null) {
            pintarFichaItem(g2, itemActual, acento, tx, h, maxTextW);
        } else {
            pintarPreviewModoNuevo(g2, cat, acento, tx, h, maxTextW);
        }

        // Borde suave redondeado
        g2.setColor(Tema.mezclar(Tema.BORDE, acento, 0.32));
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, r, r));
        g2.dispose();
    }

    private void pintarFichaItem(Graphics2D g2, ItemArsenal item, Color acento, int x, int h, int maxW) {
        // Etiqueta superior: categoría + ID + Pack-a-Punch
        String tagCat = item.categoria().getEtiqueta().toUpperCase();
        g2.setFont(Tema.ETIQUETA_MINI);
        FontMetrics fmTag = g2.getFontMetrics();
        int pillW = fmTag.stringWidth(tagCat) + 18;
        g2.setColor(Tema.alfa(acento, 38));
        g2.fillRoundRect(x, 16, pillW, 20, 20, 20);
        g2.setColor(Tema.alfa(acento, 165));
        g2.drawRoundRect(x, 16, pillW, 20, 20, 20);
        g2.setColor(Tema.mezclar(acento, Tema.PALETA_BLANCO, 0.35));
        g2.drawString(tagCat, x + 9, 16 + (20 - fmTag.getHeight()) / 2 + fmTag.getAscent());

        int nextX = x + pillW + 10;
        String idTxt = String.format("#%03d  ·  %s  ·  %s",
                item.id(), item.mapaOrigen(), item.rarezaONivel());
        g2.setFont(Tema.PEQUENA);
        g2.setColor(Tema.TEXTO_TENUE);
        g2.drawString(recortar(idTxt, g2.getFontMetrics(), maxW - pillW - 95), nextX, 30);

        if (item.packAPunch()) {
            String pap = "PACK-A-PUNCH";
            g2.setFont(Tema.ETIQUETA_MINI);
            FontMetrics fmPap = g2.getFontMetrics();
            int papW = fmPap.stringWidth(pap) + 18;
            int papX = x + Math.min(maxW - papW, pillW + 10 + g2.getFontMetrics(Tema.PEQUENA).stringWidth(idTxt) + 12);
            if (papX + papW < getWidth() - 18) {
                g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 42));
                g2.fillRoundRect(papX, 16, papW, 20, 20, 20);
                g2.setColor(Tema.PALETA_NARANJA);
                g2.drawRoundRect(papX, 16, papW, 20, 20, 20);
                g2.setColor(Tema.PALETA_BLANCO);
                g2.drawString(pap, papX + 9, 16 + (20 - fmPap.getHeight()) / 2 + fmPap.getAscent());
            }
        }

        // Nombre principal
        g2.setFont(Tema.TITULO.deriveFont(20f));
        g2.setColor(Tema.PALETA_BLANCO);
        g2.drawString(recortar(item.nombre(), g2.getFontMetrics(), maxW), x, 64);

        // Descripción breve
        String desc = item.descripcion().isBlank()
                ? "Sin descripción táctica registrada. Edita este registro en el panel izquierdo."
                : item.descripcion();
        g2.setFont(Tema.PEQUENA);
        g2.setColor(Tema.TEXTO_TENUE);
        g2.drawString(recortar(desc, g2.getFontMetrics(), maxW), x, Math.min(h - 20, 92));
    }

    private void pintarPreviewModoNuevo(Graphics2D g2, Categoria cat, Color acento, int x, int h, int maxW) {
        g2.setFont(Tema.ETIQUETA_MINI);
        g2.setColor(acento);
        g2.drawString("VISTA TÁCTICA · " + cat.getPlural().toUpperCase(), x, 30);

        g2.setFont(Tema.TITULO.deriveFont(19f));
        g2.setColor(Tema.PALETA_BLANCO);
        g2.drawString(recortar("ARMERÍA BLACK OPS III ZOMBIES", g2.getFontMetrics(), maxW), x, 62);

        String texto = switch (cat) {
            case ARMA_MARAVILLA -> "Registra Armas Maravilla icónicas (Apothicon Servant, Thundergun, Ray Gun Mk3…).";
            case GOBBLEGUM -> "Organiza tus GobbleGums Clásicas, Mega y Ultra Raras para cada partida.";
            case ARMA_ESPECIAL -> "Controla equipamiento especial como Ragnarok DG-4, Gauntlet of Siegfried o Skull.";
            case PERK_A_COLA -> "Cataloga tus Perk-a-Colas esenciales (Juggernog, Widow's Wine, Speed Cola…).";
        };
        g2.setFont(Tema.PEQUENA);
        g2.setColor(Tema.TEXTO_TENUE);
        g2.drawString(recortar(texto, g2.getFontMetrics(), maxW), x, Math.min(h - 20, 90));
    }

    private static String recortar(String texto, FontMetrics fm, int anchoMax) {
        if (anchoMax <= 20 || fm.stringWidth(texto) <= anchoMax) {
            return texto;
        }
        String puntos = "…";
        int fin = texto.length();
        while (fin > 0 && fm.stringWidth(texto.substring(0, fin) + puntos) > anchoMax) {
            fin--;
        }
        return texto.substring(0, fin) + puntos;
    }
}
