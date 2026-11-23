package com.bo3zombies.arsenal.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Set;

/** Área de texto multilínea con el mismo estilo que {@link CampoTexto}. */
public class AreaTexto extends JPanel {

    private final JTextArea area;
    private boolean error;

    public AreaTexto(int filas) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 4));

        area = new JTextArea(filas, 10);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(null);
        area.setFont(Tema.CUERPO);
        area.setForeground(Tema.TEXTO);
        area.setCaretColor(Tema.NEON);
        area.setSelectionColor(Tema.SELECCION_TEXTO);
        area.setSelectedTextColor(Color.WHITE);
        // Tab / Shift+Tab cambian de campo en lugar de insertar tabulaciones
        area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                Set.of(KeyStroke.getKeyStroke("pressed TAB")));
        area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                Set.of(KeyStroke.getKeyStroke("shift pressed TAB")));
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        JScrollPane scroll = new JScrollPane(area,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.getVerticalScrollBar().setUI(new ScrollBarOscura());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setLimite(int maximo) {
        ((AbstractDocument) area.getDocument()).setDocumentFilter(new LimiteDocumento(maximo));
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String texto) {
        area.setText(texto);
        area.setCaretPosition(0);
    }

    public Document getDocument() {
        return area.getDocument();
    }

    public JTextArea getArea() {
        return area;
    }

    public void setError(boolean error) {
        this.error = error;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        int r = Tema.RADIO_CONTROL;
        boolean foco = area.isFocusOwner();
        g2.setColor(Tema.PANEL_ALT);
        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
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
