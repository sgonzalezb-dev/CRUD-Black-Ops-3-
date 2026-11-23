package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;

import javax.swing.BorderFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Paleta oficial Black Ops 3 ({@code #F4F4F4}, {@code #060202}, {@code #FF7400},
 * {@code #696969}, {@code #ABA8A8}), tipografías y acabados suavizados.
 */
public final class Tema {

    private Tema() {
    }

    // ─────────────── Paleta oficial BO3 proporcionada ───────────────
    public static final Color PALETA_BLANCO = new Color(0xF4F4F4);      // blanco platino
    public static final Color PALETA_NEGRO = new Color(0x060202);       // negro obsidiana
    public static final Color PALETA_NARANJA = new Color(0xFF7400);     // naranja fuego BO3
    public static final Color PALETA_GRIS_OSCURO = new Color(0x696969); // gris táctico
    public static final Color PALETA_GRIS_CLARO = new Color(0xABA8A8);  // plata metálico

    // ─────────────── Superficies oscuras suavizadas ───────────────
    public static final Color FONDO = PALETA_NEGRO;
    public static final Color PANEL = new Color(0x131111);
    public static final Color PANEL_ALT = new Color(0x1C1919);
    public static final Color PANEL_FILA_ALT = new Color(0x171414);
    public static final Color PANEL_HOVER = new Color(0x231E1C);
    public static final Color PANEL_CABECERA = new Color(0x0D0A0A);
    public static final Color BORDE = new Color(0x2D2929);
    public static final Color SEPARADOR = new Color(0x221F1F);

    // ─────────────── Acentos y estados ───────────────
    public static final Color NEON = PALETA_NARANJA;                    // acento principal BO3 (#FF7400)
    public static final Color NARANJA_BRILLO = new Color(0xFF9A3C);     // brillo suave al pasar el ratón
    public static final Color AMBAR = new Color(0xFFA726);              // edición / acción secundaria
    public static final Color SANGRE = new Color(0xD83434);             // eliminar (rojo suavizado)
    public static final Color SANGRE_TEXTO = new Color(0xFF6E6E);       // texto de error legible
    public static final Color EXITO = new Color(0x4FD1A5);              // sincronización / confirmación

    // Categorías armonizadas con la paleta BO3
    public static final Color COLOR_GOBBLEGUM = new Color(0xD282FF);
    public static final Color COLOR_ESPECIAL = new Color(0xFFB74D);
    public static final Color COLOR_PERK = new Color(0x4FD1A5);

    // ─────────────── Jerarquía de texto ───────────────
    public static final Color TEXTO = PALETA_BLANCO;                    // #F4F4F4
    public static final Color TEXTO_TENUE = PALETA_GRIS_CLARO;          // #ABA8A8
    public static final Color TEXTO_APAGADO = PALETA_GRIS_OSCURO;       // #696969

    public static final Color SELECCION_FILA = mezclar(PANEL, NEON, 0.16);
    public static final Color SELECCION_LISTA = mezclar(PANEL_ALT, NEON, 0.28);
    public static final Color SELECCION_TEXTO = mezclar(PANEL_ALT, NEON, 0.42);

    // Radios de esquinas suavizados
    public static final int RADIO_PANEL = 18;
    public static final int RADIO_CONTROL = 12;
    public static final int RADIO_TARJETA = 14;

    // ─────────────── Tipografía ───────────────
    private static final Set<String> FAMILIAS = Arrays.stream(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            .collect(Collectors.toSet());

    public static final Font TITULO = espaciado(fuente(Font.PLAIN, 22f, "Segoe UI Black", "Arial Black"), 0.05f);
    public static final Font SECCION = espaciado(fuente(Font.PLAIN, 13.5f, "Segoe UI Black", "Arial Black"), 0.08f);
    public static final Font SUBTITULO = espaciado(fuente(Font.PLAIN, 11f, "Segoe UI Semibold", "Segoe UI"), 0.14f);
    public static final Font ETIQUETA = espaciado(fuente(Font.PLAIN, 10.5f, "Segoe UI Semibold", "Segoe UI"), 0.10f);
    public static final Font ETIQUETA_MINI = espaciado(fuente(Font.PLAIN, 9.5f, "Segoe UI Semibold", "Segoe UI"), 0.08f);
    public static final Font CUERPO = fuente(Font.PLAIN, 13.5f, "Segoe UI");
    public static final Font CUERPO_NEGRITA = fuente(Font.PLAIN, 13.5f, "Segoe UI Semibold", "Segoe UI");
    public static final Font PEQUENA = fuente(Font.PLAIN, 12f, "Segoe UI");
    public static final Font PEQUENA_NEGRITA = fuente(Font.BOLD, 11.5f, "Segoe UI");
    public static final Font BOTON = espaciado(fuente(Font.BOLD, 12f, "Segoe UI"), 0.10f);
    public static final Font MONO = fuente(Font.PLAIN, 12.5f, "Consolas", "Cascadia Mono");
    public static final Font NUMERO = fuente(Font.PLAIN, 22f, "Segoe UI Black", "Arial Black");

    public static Font fuente(int estilo, float tamano, String... familias) {
        for (String familia : familias) {
            if (FAMILIAS.contains(familia)) {
                return new Font(familia, estilo, 12).deriveFont(estilo, tamano);
            }
        }
        return new Font(Font.SANS_SERIF, estilo, 12).deriveFont(estilo, tamano);
    }

    public static Font espaciado(Font base, float tracking) {
        return base.deriveFont(Map.of(TextAttribute.TRACKING, tracking));
    }

    // ─────────────── Colores y utilidades ───────────────
    public static Color alfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    /** Interpola suavemente entre dos colores sólidos (t = 0 → a, t = 1 → b). */
    public static Color mezclar(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }

    public static Color colorCategoria(Categoria categoria) {
        return switch (categoria) {
            case ARMA_MARAVILLA -> PALETA_NARANJA;
            case GOBBLEGUM -> COLOR_GOBBLEGUM;
            case ARMA_ESPECIAL -> COLOR_ESPECIAL;
            case PERK_A_COLA -> COLOR_PERK;
        };
    }

    public static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }

    /** Pinta un panel contenedor con degradado sutil, luz superior suave y borde redondeado. */
    public static void pintarSuperficieSuave(Graphics2D g2, int w, int h, int radio, Color colorBorde) {
        RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radio, radio);
        g2.setPaint(new GradientPaint(0, 0, mezclar(PANEL, PANEL_ALT, 0.45), 0, h, PANEL));
        g2.fill(forma);

        // Sutil brillo superior para dar sensación de volumen suavizado
        g2.setColor(alfa(PALETA_BLANCO, 14));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(radio, 1, w - radio, 1);

        g2.setColor(colorBorde);
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(forma);
    }

    public static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    public static String html(String texto, int anchoPx) {
        return "<html><div style='width:" + anchoPx + "px'>" + escaparHtml(texto) + "</div></html>";
    }

    public static void alCambiar(Document documento, Runnable accion) {
        documento.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                accion.run();
            }
        });
    }

    /** Configura el Look&Feel multiplataforma y valores globales con la paleta BO3. */
    public static void instalarDefaults() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignorada) {
            // Se continúa con el Look&Feel actual.
        }
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Viewport.background", PANEL);
        UIManager.put("ScrollPane.background", PANEL);
        UIManager.put("ToolTip.background", PANEL_ALT);
        UIManager.put("ToolTip.foreground", TEXTO);
        UIManager.put("ToolTip.font", PEQUENA);
        UIManager.put("ToolTip.border", new CompoundBorder(
                new LineBorder(mezclar(BORDE, NEON, 0.55)), new EmptyBorder(7, 10, 7, 10)));
        UIManager.put("ComboBox.background", PANEL_ALT);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("ComboBox.selectionBackground", SELECCION_LISTA);
        UIManager.put("ComboBox.selectionForeground", PALETA_BLANCO);
        UIManager.put("ComboBox.disabledBackground", PANEL);
        UIManager.put("ComboBox.disabledForeground", TEXTO_APAGADO);
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.width", 10);
        ToolTipManager.sharedInstance().setInitialDelay(400);
        ToolTipManager.sharedInstance().setDismissDelay(12000);
    }
}
