package com.bo3zombies.arsenal.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.KeyEvent;

/** Diálogo modal oscuro con acento neón (reemplaza al JOptionPane gris del sistema). */
public final class DialogoNeon extends JDialog {

    private boolean confirmado;

    private DialogoNeon(Window dueno, String titulo, String mensaje, Color acento,
                        String textoConfirmar, String textoCancelar) {
        super(dueno, titulo, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel contenedor = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suavizar(g);
                int w = getWidth();
                int h = getHeight();
                Tema.pintarSuperficieSuave(g2, w, h, Tema.RADIO_PANEL, Tema.mezclar(Tema.BORDE, acento, 0.55));
                // Barra superior suavizada de acento
                g2.setColor(acento);
                g2.fillRoundRect(24, 1, w - 48, 3, 3, 3);
                g2.dispose();
            }
        };
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(22, 24, 20, 24));

        // Cabecera
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cabecera.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.SECCION);
        lblTitulo.setForeground(acento);
        lblTitulo.setIcon(new IconoPunto(acento, 10));
        lblTitulo.setIconTextGap(10);
        cabecera.add(lblTitulo);
        contenedor.add(cabecera, BorderLayout.NORTH);

        // Cuerpo
        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        JLabel lblMensaje = new JLabel(Tema.html(mensaje, 380));
        lblMensaje.setFont(Tema.CUERPO);
        lblMensaje.setForeground(Tema.TEXTO);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuerpo.add(lblMensaje);
        cuerpo.add(Box.createVerticalStrut(4));
        contenedor.add(cuerpo, BorderLayout.CENTER);

        // Botones
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.SEPARADOR));
        JPanel botonesWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesWrap.setOpaque(false);
        botonesWrap.setBorder(new EmptyBorder(14, 0, 0, 0));

        if (textoCancelar != null) {
            BotonNeon btnCancelar = new BotonNeon(textoCancelar, Tema.TEXTO_TENUE);
            btnCancelar.addActionListener(e -> {
                confirmado = false;
                dispose();
            });
            botonesWrap.add(btnCancelar);
        }

        BotonNeon btnAceptar = new BotonNeon(textoConfirmar, acento, true);
        btnAceptar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        botonesWrap.add(btnAceptar);
        pie.add(botonesWrap);
        contenedor.add(pie, BorderLayout.SOUTH);

        setContentPane(contenedor);
        getRootPane().setDefaultButton(btnAceptar);
        getRootPane().registerKeyboardAction(
                e -> {
                    confirmado = false;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(dueno);
    }

    /** Muestra un aviso informativo o de error. */
    public static void mostrar(Window dueno, String titulo, String mensaje, Color acento) {
        DialogoNeon dialogo = new DialogoNeon(dueno, titulo, mensaje, acento, "ENTENDIDO", null);
        dialogo.setVisible(true);
    }

    /** Muestra un cuadro de confirmación y devuelve {@code true} solo si el usuario confirma. */
    public static boolean confirmar(Window dueno, String titulo, String mensaje,
                                    Color acento, String textoConfirmar) {
        DialogoNeon dialogo = new DialogoNeon(dueno, titulo, mensaje, acento, textoConfirmar, "CANCELAR");
        dialogo.setVisible(true);
        return dialogo.confirmado;
    }
}
