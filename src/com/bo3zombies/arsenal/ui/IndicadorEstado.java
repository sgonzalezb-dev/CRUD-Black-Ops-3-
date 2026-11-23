package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.service.EstadoGuardado;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Barra de estado inferior: indica si los datos están sincronizados en el archivo local. */
public class IndicadorEstado extends JPanel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PuntoPulso punto;
    private final JLabel lblEstado;
    private final JLabel lblDetalle;
    private final JLabel lblRuta;

    public IndicadorEstado(Path rutaArchivo) {
        super(new BorderLayout(16, 0));
        setBackground(Tema.PANEL_CABECERA);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                new EmptyBorder(8, 18, 8, 18)));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);

        punto = new PuntoPulso();
        lblEstado = new JLabel();
        lblEstado.setFont(Tema.PEQUENA_NEGRITA);

        lblDetalle = new JLabel();
        lblDetalle.setFont(Tema.PEQUENA);
        lblDetalle.setForeground(Tema.TEXTO_TENUE);

        izquierda.add(punto);
        izquierda.add(lblEstado);
        izquierda.add(lblDetalle);

        lblRuta = new JLabel(acortarRuta(rutaArchivo));
        lblRuta.setFont(Tema.MONO.deriveFont(11.5f));
        lblRuta.setForeground(Tema.TEXTO_APAGADO);
        lblRuta.setToolTipText(rutaArchivo.toString());

        add(izquierda, BorderLayout.WEST);
        add(lblRuta, BorderLayout.EAST);
    }

    public void actualizar(EstadoGuardado estado, int totalRegistros, LocalDateTime ultimaSync) {
        String registros = totalRegistros == 1 ? "1 registro" : totalRegistros + " registros";
        String hora = ultimaSync != null ? " · " + ultimaSync.format(HORA) : "";

        switch (estado) {
            case SIN_ARCHIVO -> {
                punto.setColor(Tema.TEXTO_TENUE, false);
                lblEstado.setForeground(Tema.TEXTO_TENUE);
                lblEstado.setText("Sin archivo local aún (se creará al guardar)");
                lblDetalle.setText("· " + registros);
            }
            case GUARDANDO -> {
                punto.setColor(Tema.AMBAR, true);
                lblEstado.setForeground(Tema.AMBAR);
                lblEstado.setText("Guardando en archivo local…");
                lblDetalle.setText("· " + registros);
            }
            case SINCRONIZADO -> {
                punto.setColor(Tema.EXITO, true);
                lblEstado.setForeground(Tema.EXITO);
                lblEstado.setText("Sincronizado en archivo local");
                lblDetalle.setText("· " + registros + hora);
            }
            case ERROR -> {
                punto.setColor(Tema.SANGRE_TEXTO, true);
                lblEstado.setForeground(Tema.SANGRE_TEXTO);
                lblEstado.setText("Error al guardar — cambio revertido");
                lblDetalle.setText("· " + registros);
            }
        }
    }

    private static String acortarRuta(Path ruta) {
        String texto = ruta.toString();
        int max = 56;
        if (texto.length() <= max) {
            return texto;
        }
        return "…" + texto.substring(texto.length() - (max - 1));
    }

    /** Pequeño LED circular que emite un pulso breve tras cada guardado. */
    private static final class PuntoPulso extends JPanel {
        private Color color = Tema.NEON;
        private float pulso;
        private final Timer timer;

        PuntoPulso() {
            setOpaque(false);
            setPreferredSize(new Dimension(14, 14));
            timer = new Timer(25, e -> {
                pulso -= 0.06f;
                if (pulso <= 0f) {
                    pulso = 0f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        void setColor(Color color, boolean pulsar) {
            this.color = color;
            if (pulsar) {
                this.pulso = 1f;
                timer.restart();
            } else {
                this.pulso = 0f;
                timer.stop();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            if (pulso > 0f) {
                float r = 4f + (1f - pulso) * 3f;
                g2.setColor(Tema.alfa(color, Math.round(140 * pulso)));
                g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
            }
            g2.setColor(Tema.alfa(color, 70));
            g2.fill(new Ellipse2D.Float(cx - 5f, cy - 5f, 10f, 10f));
            g2.setColor(color);
            g2.fill(new Ellipse2D.Float(cx - 3.2f, cy - 3.2f, 6.4f, 6.4f));
            g2.dispose();
        }
    }
}
