package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.AbstractDocument;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;

/** JComboBox oscuro estilizado con flecha personalizada, popup oscuro y borde neón al enfocar. */
public class ComboOscuro<E> extends JComboBox<E> {

    private boolean error;
    private boolean enfocado;

    public ComboOscuro() {
        super();
        configurar();
    }

    public ComboOscuro(E[] items) {
        super(items);
        configurar();
    }

    private void configurar() {
        setOpaque(false);
        setFont(Tema.CUERPO);
        setForeground(Tema.TEXTO);
        setBackground(Tema.PANEL_ALT);
        setBorder(new EmptyBorder(2, 6, 2, 2));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumRowCount(9);

        setUI(new ComboOscuroUI());
        setRenderer(new RendererOscuro());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enfocado = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                enfocado = false;
                repaint();
            }
        });
    }

    /** Activa modo editable con un campo de texto estilizado y límite de caracteres opcional. */
    public void hacerEditable(int limiteCaracteres) {
        setEditable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        EditorOscuro editorOscuro = new EditorOscuro(limiteCaracteres);
        setEditor(editorOscuro);
    }

    public String getTextoEditor() {
        if (isEditable()) {
            Object val = getEditor().getItem();
            return val == null ? "" : val.toString();
        }
        Object sel = getSelectedItem();
        return sel == null ? "" : sel.toString();
    }

    public void setTextoEditor(String texto) {
        if (isEditable()) {
            getEditor().setItem(texto == null ? "" : texto);
        }
        setSelectedItem(texto);
    }

    public void setError(boolean error) {
        this.error = error;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(40, d.height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();

        int r = Tema.RADIO_CONTROL;
        g2.setColor(isEnabled() ? Tema.PANEL_ALT : Tema.PANEL);
        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
        g2.dispose();

        super.paintComponent(g);

        Graphics2D b = Tema.suavizar(g);
        boolean activo = enfocado || isPopupVisible();
        Color borde = error ? Tema.SANGRE : activo ? Tema.NEON : Tema.BORDE;
        if (activo || error) {
            b.setColor(Tema.alfa(borde, 38));
            b.setStroke(new BasicStroke(3.2f));
            b.drawRoundRect(1, 1, w - 3, h - 3, r, r);
        }
        b.setColor(borde);
        b.setStroke(new BasicStroke(1.1f));
        b.drawRoundRect(0, 0, w - 1, h - 1, r, r);
        b.dispose();
    }

    private final class EditorOscuro extends BasicComboBoxEditor {
        EditorOscuro(int limite) {
            super();
            editor.setOpaque(false);
            editor.setFont(Tema.CUERPO);
            editor.setForeground(Tema.TEXTO);
            editor.setCaretColor(Tema.NEON);
            editor.setSelectionColor(Tema.SELECCION_TEXTO);
            editor.setSelectedTextColor(Color.WHITE);
            editor.setBorder(new EmptyBorder(0, 8, 0, 4));
            if (limite > 0) {
                ((AbstractDocument) editor.getDocument()).setDocumentFilter(new LimiteDocumento(limite));
            }
            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    enfocado = true;
                    ComboOscuro.this.repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    enfocado = false;
                    ComboOscuro.this.repaint();
                }
            });
            Tema.alCambiar(editor.getDocument(), () -> {
                if (error) {
                    setError(false);
                }
            });
        }

        @Override
        protected JTextField createEditorComponent() {
            return new JTextField();
        }
    }

    private final class ComboOscuroUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton boton = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Tema.suavizar(g);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    boolean abierto = ComboOscuro.this.isPopupVisible();
                    g2.setColor(abierto || enfocado ? Tema.NEON : Tema.TEXTO_TENUE);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D flecha = new Path2D.Float();
                    if (abierto) {
                        flecha.moveTo(cx - 4.5f, cy + 2f);
                        flecha.lineTo(cx, cy - 2.5f);
                        flecha.lineTo(cx + 4.5f, cy + 2f);
                    } else {
                        flecha.moveTo(cx - 4.5f, cy - 2f);
                        flecha.lineTo(cx, cy + 2.5f);
                        flecha.lineTo(cx + 4.5f, cy - 2f);
                    }
                    g2.draw(flecha);
                    g2.dispose();
                }
            };
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return boton;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // El fondo redondeado se pinta en ComboOscuro.paintComponent
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            super.paintCurrentValue(g, bounds, false);
        }

        @Override
        protected Rectangle rectangleForCurrentValue() {
            int w = comboBox.getWidth();
            int h = comboBox.getHeight();
            Insets in = getInsets();
            int btnW = 30;
            return new Rectangle(in.left + 4, in.top, w - in.left - in.right - btnW - 4, h - in.top - in.bottom);
        }

        @Override
        protected void configureEditor() {
            ComboBoxEditor ed = comboBox.getEditor();
            if (ed != null && ed.getEditorComponent() instanceof JComponent jc) {
                jc.setFont(Tema.CUERPO);
            }
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setBorder(BorderFactory.createLineBorder(Tema.mezclar(Tema.BORDE, Tema.NEON, 0.55), 1));
                    setBackground(Tema.PANEL_ALT);
                }

                @Override
                protected JScrollPane createScroller() {
                    JScrollPane sp = super.createScroller();
                    ScrollBarOscura.aplicar(sp, Tema.PANEL_ALT);
                    return sp;
                }

                @Override
                protected void configureList() {
                    super.configureList();
                    list.setBackground(Tema.PANEL_ALT);
                    list.setForeground(Tema.TEXTO);
                    list.setSelectionBackground(Tema.SELECCION_LISTA);
                    list.setSelectionForeground(Color.WHITE);
                    list.setFixedCellHeight(32);
                }
            };
        }
    }

    private static final class RendererOscuro extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
            lbl.setFont(Tema.CUERPO);
            lbl.setBorder(new EmptyBorder(4, index == -1 ? 4 : 10, 4, 10));
            lbl.setOpaque(index != -1);

            if (index == -1) {
                lbl.setForeground(Tema.TEXTO);
                lbl.setBackground(Tema.PANEL_ALT);
            } else if (isSelected) {
                lbl.setBackground(Tema.SELECCION_LISTA);
                lbl.setForeground(Color.WHITE);
            } else {
                lbl.setBackground(Tema.PANEL_ALT);
                lbl.setForeground(Tema.TEXTO);
            }

            if (value instanceof Categoria cat) {
                lbl.setIcon(new IconoPunto(Tema.colorCategoria(cat), 8));
                lbl.setIconTextGap(8);
            } else {
                lbl.setIcon(null);
            }
            return lbl;
        }
    }
}
