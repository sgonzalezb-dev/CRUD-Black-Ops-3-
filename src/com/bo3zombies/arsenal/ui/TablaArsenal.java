package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Tabla estilizada para el arsenal: filas alternas oscuras, efecto hover,
 * selección con barra neón a la izquierda, etiquetas de color para categorías
 * y ordenación/filtrado en vivo.
 */
public class TablaArsenal extends JTable {

    private final ArsenalTableModel modelo;
    private final TableRowSorter<ArsenalTableModel> ordenador;
    private int filaHover = -1;
    private String textoFiltro = "";
    private Categoria categoriaFiltro;

    public TablaArsenal(ArsenalTableModel modelo) {
        super(modelo);
        this.modelo = modelo;

        setFillsViewportHeight(true);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(38);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(Tema.PANEL);
        setForeground(Tema.TEXTO);
        setSelectionBackground(Tema.SELECCION_FILA);
        setSelectionForeground(Color.WHITE);
        setFont(Tema.CUERPO);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        ordenador = new TableRowSorter<>(modelo);
        ordenador.setComparator(ArsenalTableModel.COL_CATEGORIA,
                Comparator.comparing(Categoria::getEtiqueta, String.CASE_INSENSITIVE_ORDER));
        setRowSorter(ordenador);

        configurarCabecera();
        configurarColumnas();
        instalarHover();
    }

    private void configurarCabecera() {
        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setBackground(Tema.PANEL_CABECERA);
        header.setForeground(Tema.TEXTO_TENUE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setDefaultRenderer(new RendererCabecera());
    }

    private void configurarColumnas() {
        TableColumnModel cm = getColumnModel();
        cm.getColumn(ArsenalTableModel.COL_ID).setPreferredWidth(58);
        cm.getColumn(ArsenalTableModel.COL_ID).setMaxWidth(76);
        cm.getColumn(ArsenalTableModel.COL_NOMBRE).setPreferredWidth(175);
        cm.getColumn(ArsenalTableModel.COL_CATEGORIA).setPreferredWidth(148);
        cm.getColumn(ArsenalTableModel.COL_MAPA).setPreferredWidth(148);
        cm.getColumn(ArsenalTableModel.COL_RAREZA).setPreferredWidth(122);
        cm.getColumn(ArsenalTableModel.COL_PAP).setPreferredWidth(124);
        cm.getColumn(ArsenalTableModel.COL_PAP).setMaxWidth(145);
        cm.getColumn(ArsenalTableModel.COL_DESCRIPCION).setPreferredWidth(235);

        cm.getColumn(ArsenalTableModel.COL_ID).setCellRenderer(new RendererId());
        cm.getColumn(ArsenalTableModel.COL_NOMBRE).setCellRenderer(new RendererTexto(true, Tema.TEXTO));
        cm.getColumn(ArsenalTableModel.COL_CATEGORIA).setCellRenderer(new RendererCategoria());
        cm.getColumn(ArsenalTableModel.COL_MAPA).setCellRenderer(new RendererTexto(false, Tema.TEXTO));
        cm.getColumn(ArsenalTableModel.COL_RAREZA).setCellRenderer(new RendererTexto(false, Tema.TEXTO_TENUE));
        cm.getColumn(ArsenalTableModel.COL_PAP).setCellRenderer(new RendererPackAPunch());
        cm.getColumn(ArsenalTableModel.COL_DESCRIPCION).setCellRenderer(new RendererTexto(false, Tema.TEXTO_TENUE));
    }

    private void instalarHover() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = rowAtPoint(e.getPoint());
                if (fila != filaHover) {
                    filaHover = fila;
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (filaHover != -1) {
                    filaHover = -1;
                    repaint();
                }
            }
        });
    }

    /** Aplica el filtro combinado de búsqueda textual (sin distinguir mayúsculas) y categoría. */
    public void aplicarFiltro(String texto, Categoria categoria) {
        this.textoFiltro = texto == null ? "" : texto.strip();
        this.categoriaFiltro = categoria;

        List<RowFilter<ArsenalTableModel, Integer>> filtros = new ArrayList<>();
        if (!textoFiltro.isEmpty()) {
            String regex = "(?i)" + Pattern.quote(textoFiltro);
            filtros.add(RowFilter.regexFilter(regex,
                    ArsenalTableModel.COL_ID,
                    ArsenalTableModel.COL_NOMBRE,
                    ArsenalTableModel.COL_MAPA,
                    ArsenalTableModel.COL_RAREZA,
                    ArsenalTableModel.COL_DESCRIPCION));
        }
        if (categoriaFiltro != null) {
            filtros.add(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends ArsenalTableModel, ? extends Integer> entry) {
                    return categoriaFiltro.equals(entry.getValue(ArsenalTableModel.COL_CATEGORIA));
                }
            });
        }
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    public Optional<ItemArsenal> getItemSeleccionado() {
        int filaVista = getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        int filaModelo = convertRowIndexToModel(filaVista);
        return Optional.of(modelo.getItemEn(filaModelo));
    }

    /** Selecciona la fila del ítem indicado (si es visible tras el filtro) y hace scroll hasta ella. */
    public void seleccionarPorId(int id) {
        int filaModelo = modelo.indicePorId(id);
        if (filaModelo < 0) {
            return;
        }
        int filaVista = convertRowIndexToView(filaModelo);
        if (filaVista >= 0) {
            setRowSelectionInterval(filaVista, filaVista);
            scrollRectToVisible(getCellRect(filaVista, 0, true));
        }
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        int fila = rowAtPoint(p);
        int col = columnAtPoint(p);
        if (fila < 0 || col < 0) {
            return null;
        }
        int colModelo = convertColumnIndexToModel(col);
        if (colModelo == ArsenalTableModel.COL_DESCRIPCION || colModelo == ArsenalTableModel.COL_NOMBRE) {
            Object val = getValueAt(fila, col);
            if (val != null) {
                String txt = val.toString();
                if (!txt.isBlank()) {
                    return Tema.html(txt, 320);
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Barra vertical naranja BO3 suavizada a la izquierda de la fila seleccionada
        int sel = getSelectedRow();
        if (sel >= 0) {
            Rectangle r = getCellRect(sel, 0, true);
            Graphics2D g2 = Tema.suavizar(g);
            g2.setColor(Tema.NEON);
            g2.fillRoundRect(2, r.y + 5, 3, r.height - 10, 3, 3);
            g2.dispose();
        }

        // Estado vacío con marca de agua tenue del logo BO3
        if (getRowCount() == 0) {
            Graphics2D g2 = Tema.suavizar(g);
            boolean hayDatos = modelo.getRowCount() > 0;
            String titulo = hayDatos
                    ? "SIN COINCIDENCIAS EN EL ARSENAL"
                    : "ARSENAL VACÍO — LISTO PARA DESPLIEGUE";
            String subtitulo = hayDatos
                    ? "Prueba con otro término de búsqueda o cambia el filtro de categoría."
                    : "Usa el formulario de la izquierda para registrar Armas Maravilla, GobbleGums o Perks.";

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            var logo = Recursos.logoBO3();
            if (logo != null) {
                int logoTam = Math.min(130, Math.max(76, getHeight() - 90));
                Recursos.pintarCover(g2, logo, cx - logoTam / 2, cy - logoTam / 2 - 4,
                        logoTam, logoTam, 20, 0.14f);
            }

            g2.setFont(Tema.SECCION);
            g2.setColor(hayDatos ? Tema.AMBAR : Tema.NEON);
            FontMetrics fm1 = g2.getFontMetrics();
            g2.drawString(titulo, cx - fm1.stringWidth(titulo) / 2, cy - 6);

            g2.setFont(Tema.PEQUENA);
            g2.setColor(Tema.TEXTO_TENUE);
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(subtitulo, cx - fm2.stringWidth(subtitulo) / 2, cy + 18);
            g2.dispose();
        }
    }

    private Color colorFondoFila(int fila, boolean seleccionada) {
        if (seleccionada) {
            return Tema.SELECCION_FILA;
        }
        if (fila == filaHover) {
            return Tema.PANEL_HOVER;
        }
        return fila % 2 == 0 ? Tema.PANEL : Tema.PANEL_FILA_ALT;
    }

    // ─────────────────────────── Renderers ───────────────────────────

    private final class RendererCabecera extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel(value == null ? "" : value.toString()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Línea inferior neón sutil
                    g.setColor(Tema.alfa(Tema.NEON, 110));
                    g.fillRect(0, getHeight() - 2, getWidth(), 2);
                }
            };
            lbl.setOpaque(true);
            lbl.setBackground(Tema.PANEL_CABECERA);
            lbl.setForeground(Tema.TEXTO_TENUE);
            lbl.setFont(Tema.ETIQUETA);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));

            int colModelo = convertColumnIndexToModel(column);
            if (colModelo == ArsenalTableModel.COL_ID || colModelo == ArsenalTableModel.COL_PAP) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }

            // Flecha de ordenación si esta columna está ordenada
            List<? extends RowSorter.SortKey> keys = ordenador.getSortKeys();
            if (!keys.isEmpty() && keys.get(0).getColumn() == colModelo) {
                SortOrder orden = keys.get(0).getSortOrder();
                if (orden == SortOrder.ASCENDING) {
                    lbl.setText(lbl.getText() + "  ▲");
                    lbl.setForeground(Tema.NEON);
                } else if (orden == SortOrder.DESCENDING) {
                    lbl.setText(lbl.getText() + "  ▼");
                    lbl.setForeground(Tema.NEON);
                }
            }
            return lbl;
        }
    }

    private final class RendererId extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            int id = value instanceof Integer n ? n : 0;
            lbl.setText(String.format("#%03d", id));
            lbl.setFont(Tema.MONO);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setForeground(isSelected ? Tema.NEON : Tema.TEXTO_TENUE);
            lbl.setBackground(colorFondoFila(row, isSelected));
            lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.SEPARADOR));
            return lbl;
        }
    }

    private final class RendererTexto extends DefaultTableCellRenderer {
        private final boolean negrita;
        private final Color colorBase;

        RendererTexto(boolean negrita, Color colorBase) {
            this.negrita = negrita;
            this.colorBase = colorBase;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            String txt = value == null ? "" : value.toString();
            lbl.setText(txt.isEmpty() ? "—" : txt);
            lbl.setFont(negrita ? Tema.CUERPO_NEGRITA : Tema.CUERPO);
            lbl.setForeground(isSelected ? Color.WHITE : (txt.isEmpty() ? Tema.TEXTO_APAGADO : colorBase));
            lbl.setBackground(colorFondoFila(row, isSelected));
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.SEPARADOR),
                    new EmptyBorder(0, 12, 0, 12)));
            return lbl;
        }
    }

    private final class RendererCategoria extends JPanel implements TableCellRenderer {
        private Categoria categoria;
        private Color fondo;

        RendererCategoria() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            this.categoria = value instanceof Categoria c ? c : Categoria.ARMA_MARAVILLA;
            this.fondo = colorFondoFila(row, isSelected);
            setBackground(fondo);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();

            // Línea separadora inferior
            g2.setColor(Tema.SEPARADOR);
            g2.drawLine(0, h - 1, w, h - 1);

            if (categoria != null) {
                Color c = Tema.colorCategoria(categoria);
                String texto = categoria.getEtiqueta();
                g2.setFont(Tema.PEQUENA_NEGRITA);
                FontMetrics fm = g2.getFontMetrics();

                int pillW = Math.min(w - 16, fm.stringWidth(texto) + 26);
                int pillH = 24;
                int px = 10;
                int py = (h - pillH) / 2;

                g2.setColor(Tema.alfa(c, 28));
                g2.fillRoundRect(px, py, pillW, pillH, pillH, pillH);
                g2.setColor(Tema.alfa(c, 150));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(px, py, pillW, pillH, pillH, pillH);

                // Punto de color
                g2.setColor(c);
                g2.fillOval(px + 8, py + (pillH - 6) / 2, 6, 6);

                // Etiqueta
                g2.setColor(Tema.mezclar(c, Color.WHITE, 0.35));
                g2.drawString(texto, px + 18, py + (pillH - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private final class RendererPackAPunch extends JPanel implements TableCellRenderer {
        private boolean activo;

        RendererPackAPunch() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            this.activo = Boolean.TRUE.equals(value);
            setBackground(colorFondoFila(row, isSelected));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(Tema.SEPARADOR);
            g2.drawLine(0, h - 1, w, h - 1);

            if (activo) {
                String txt = "ACTIVO";
                g2.setFont(Tema.ETIQUETA_MINI);
                FontMetrics fm = g2.getFontMetrics();
                int pillW = fm.stringWidth(txt) + 26;
                int pillH = 22;
                int px = (w - pillW) / 2;
                int py = (h - pillH) / 2;

                g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 34));
                g2.fillRoundRect(px, py, pillW, pillH, pillH, pillH);
                g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(px, py, pillW, pillH, pillH, pillH);

                // Rayo vectorial limpio (sin depender de emojis de fuente)
                int rx = px + 9;
                int ry = py + 5;
                g2.setColor(Tema.PALETA_NARANJA);
                int[] bx = {rx + 4, rx, rx + 3, rx + 1, rx + 7, rx + 4};
                int[] by = {ry, ry + 7, ry + 7, ry + 12, ry + 5, ry + 5};
                g2.fillPolygon(bx, by, 6);

                g2.setColor(Tema.PALETA_BLANCO);
                g2.drawString(txt, px + 20, py + (pillH - fm.getHeight()) / 2 + fm.getAscent());
            } else {
                g2.setFont(Tema.CUERPO);
                g2.setColor(Tema.TEXTO_APAGADO);
                FontMetrics fm = g2.getFontMetrics();
                String guion = "—";
                g2.drawString(guion, (w - fm.stringWidth(guion)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}
