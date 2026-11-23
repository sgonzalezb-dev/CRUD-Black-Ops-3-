package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cargador y caché de imágenes del juego (logo BO3, banner atmosférico y artes por categoría).
 * <p>
 * Busca primero en el classpath ({@code /images/...}) y, si no está empaquetado,
 * en la carpeta local {@code resources/images/...}. Nunca lanza excepciones si
 * falta un archivo: simplemente devuelve {@code null} y la interfaz usa respaldo vectorial.
 */
public final class Recursos {

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static final BufferedImage AUSENTE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private Recursos() {
    }

    public static BufferedImage logoBO3() {
        return cargar("logo_bo3.png");
    }

    public static BufferedImage bannerZombies() {
        return cargar("banner_bo3_zombies.png");
    }

    public static BufferedImage arteCategoria(Categoria categoria) {
        if (categoria == null) {
            return bannerZombies();
        }
        String archivo = switch (categoria) {
            case ARMA_MARAVILLA -> "cat_arma_maravilla.png";
            case GOBBLEGUM -> "cat_gobblegum.png";
            case ARMA_ESPECIAL -> "cat_arma_especial.png";
            case PERK_A_COLA -> "cat_perk_a_cola.png";
        };
        BufferedImage img = cargar(archivo);
        return img != null ? img : bannerZombies();
    }

    public static BufferedImage cargar(String nombreArchivo) {
        BufferedImage img = CACHE.computeIfAbsent(nombreArchivo, Recursos::leerImagen);
        return img == AUSENTE ? null : img;
    }

    private static BufferedImage leerImagen(String nombre) {
        try (InputStream is = Recursos.class.getResourceAsStream("/images/" + nombre)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    return img;
                }
            }
        } catch (IOException ignorada) {
            // Se intenta desde disco.
        }
        Path ruta = Path.of("resources", "images", nombre);
        if (Files.exists(ruta)) {
            try {
                BufferedImage img = ImageIO.read(ruta.toFile());
                if (img != null) {
                    return img;
                }
            } catch (IOException ignorada) {
                // Sin imagen disponible.
            }
        }
        return AUSENTE;
    }

    /**
     * Dibuja una imagen cubriendo por completo el rectángulo (modo "cover", sin
     * deformarla) y recortada con esquinas redondeadas suaves.
     */
    public static void pintarCover(Graphics2D g2, BufferedImage img, int x, int y,
                                   int w, int h, int radio, float opacidad) {
        if (img == null || w <= 0 || h <= 0 || opacidad <= 0f) {
            return;
        }
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (radio > 0) {
            g.clip(new RoundRectangle2D.Float(x, y, w, h, radio, radio));
        } else {
            g.clipRect(x, y, w, h);
        }
        if (opacidad < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        }

        double escalaX = (double) w / img.getWidth();
        double escalaY = (double) h / img.getHeight();
        double escala = Math.max(escalaX, escalaY);
        int dw = (int) Math.ceil(img.getWidth() * escala);
        int dh = (int) Math.ceil(img.getHeight() * escala);
        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;

        g.drawImage(img, dx, dy, dw, dh, null);
        g.dispose();
    }

    /** Versión escalada suavemente para el icono de la ventana / barra de tareas. */
    public static Image iconoVentana() {
        BufferedImage logo = logoBO3();
        if (logo == null) {
            return null;
        }
        return logo.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
    }
}
