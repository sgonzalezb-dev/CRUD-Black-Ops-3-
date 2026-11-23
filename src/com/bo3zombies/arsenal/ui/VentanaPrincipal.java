package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;
import com.bo3zombies.arsenal.service.ArsenalService;
import com.bo3zombies.arsenal.service.EstadoGuardado;
import com.bo3zombies.arsenal.service.PersistenciaException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Ventana principal de la aplicación Black Ops III · Zombies Arsenal. */
public class VentanaPrincipal extends JFrame {

    private static final String FILTRO_TODAS = "Todas las categorías";

    private final ArsenalService servicio;
    private final ArsenalTableModel modeloTabla;
    private final TablaArsenal tabla;
    private final PanelFormulario formulario;
    private final TarjetaDossierVisual dossierVisual;
    private final CampoTexto txtBuscar;
    private final ComboOscuro<Object> cmbFiltroCategoria;
    private final JLabel lblResumenFiltro;
    private final IndicadorEstado indicadorEstado;
    private final Map<Categoria, TarjetaContador> tarjetas = new EnumMap<>(Categoria.class);

    public VentanaPrincipal(ArsenalService servicio) {
        super("Call of Duty: Black Ops III · Zombies Arsenal");
        this.servicio = servicio;

        Image icono = Recursos.iconoVentana();
        if (icono != null) {
            setIconImage(icono);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 820);
        setMinimumSize(new Dimension(1140, 680));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(Tema.FONDO);
        setContentPane(raiz);

        // ───────── NORTH: Encabezado cinematográfico con logo BO3 + contadores ─────────
        raiz.add(crearEncabezado(), BorderLayout.NORTH);

        // ───────── CENTER: Formulario lateral + columna derecha (Dossier + Tabla) ─────────
        JPanel cuerpo = new JPanel(new BorderLayout(18, 0));
        cuerpo.setBackground(Tema.FONDO);
        cuerpo.setBorder(new EmptyBorder(18, 20, 18, 20));

        formulario = new PanelFormulario();
        cuerpo.add(formulario, BorderLayout.WEST);

        // Columna derecha: Dossier visual arriba + Panel de tabla abajo
        JPanel columnaDerecha = new JPanel(new BorderLayout(0, 14));
        columnaDerecha.setOpaque(false);

        dossierVisual = new TarjetaDossierVisual();
        columnaDerecha.add(dossierVisual, BorderLayout.NORTH);

        JPanel panelTabla = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suavizar(g);
                Tema.pintarSuperficieSuave(g2, getWidth(), getHeight(), Tema.RADIO_PANEL, Tema.BORDE);
                g2.dispose();
            }
        };
        panelTabla.setOpaque(false);
        panelTabla.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Barra de búsqueda y filtro
        JPanel barraFiltro = new JPanel(new BorderLayout(12, 0));
        barraFiltro.setOpaque(false);

        txtBuscar = new CampoTexto("Buscar por nombre, mapa, rareza o descripción… (Ctrl+F)", true);
        txtBuscar.setPreferredSize(new Dimension(320, 40));

        JPanel derechaFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derechaFiltro.setOpaque(false);

        cmbFiltroCategoria = new ComboOscuro<>();
        cmbFiltroCategoria.addItem(FILTRO_TODAS);
        for (Categoria c : Categoria.values()) {
            cmbFiltroCategoria.addItem(c);
        }
        cmbFiltroCategoria.setPreferredSize(new Dimension(210, 40));

        lblResumenFiltro = new JLabel();
        lblResumenFiltro.setFont(Tema.PEQUENA);
        lblResumenFiltro.setForeground(Tema.TEXTO_TENUE);

        derechaFiltro.add(cmbFiltroCategoria);
        derechaFiltro.add(lblResumenFiltro);

        barraFiltro.add(txtBuscar, BorderLayout.CENTER);
        barraFiltro.add(derechaFiltro, BorderLayout.EAST);
        panelTabla.add(barraFiltro, BorderLayout.NORTH);

        // Tabla estilizada
        modeloTabla = new ArsenalTableModel();
        tabla = new TablaArsenal(modeloTabla);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        ScrollBarOscura.aplicar(scrollTabla, Tema.PANEL);
        scrollTabla.setBorder(BorderFactory.createLineBorder(Tema.SEPARADOR, 1));
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        columnaDerecha.add(panelTabla, BorderLayout.CENTER);
        cuerpo.add(columnaDerecha, BorderLayout.CENTER);
        raiz.add(cuerpo, BorderLayout.CENTER);

        // ───────── SOUTH: Barra de estado de persistencia ─────────
        indicadorEstado = new IndicadorEstado(servicio.getRutaArchivo());
        raiz.add(indicadorEstado, BorderLayout.SOUTH);

        // ───────── Conectar eventos y estado inicial ─────────
        conectarEventos();
        instalarAtajos();

        EstadoGuardado estadoInicial = servicio.existeArchivo()
                ? EstadoGuardado.SINCRONIZADO
                : EstadoGuardado.SIN_ARCHIVO;
        refrescarVista(null);
        indicadorEstado.actualizar(estadoInicial, servicio.cantidad(), servicio.getUltimaSincronizacion());
    }

    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = Tema.suavizar(g);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(Tema.PANEL_CABECERA);
                g2.fillRect(0, 0, w, h);

                // Fondo atmosférico del juego
                BufferedImage banner = Recursos.bannerZombies();
                if (banner != null) {
                    Recursos.pintarCover(g2, banner, 0, 0, w, h, 0, 0.88f);
                }

                // Fundido inferior suave hacia el fondo obsidiana
                g2.setPaint(new GradientPaint(0, h * 0.45f, Tema.alfa(Tema.FONDO, 0),
                        0, h, Tema.alfa(Tema.FONDO, 190)));
                g2.fillRect(0, 0, w, h);

                // Línea inferior con degradado naranja fuego BO3 → gris táctico
                g2.setPaint(new GradientPaint(0, 0, Tema.PALETA_NARANJA, w * 0.55f, 0, Tema.BORDE));
                g2.fillRect(0, h - 2, w, 2);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 22, 16, 22));

        // Izquierda: Logo oficial BO3 "III" + título
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        izquierda.setOpaque(false);
        izquierda.add(new LogoBO3Panel());

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        TituloBO3 lblTitulo = new TituloBO3("BLACK OPS III", " · ZOMBIES ARSENAL");
        JLabel lblSubtitulo = new JLabel("ARMAS MARAVILLA  ·  GOBBLEGUMS  ·  EQUIPAMIENTO ESPECIAL  ·  PERK-A-COLAS");
        lblSubtitulo.setFont(Tema.SUBTITULO);
        lblSubtitulo.setForeground(Tema.TEXTO_TENUE);

        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(lblSubtitulo);
        izquierda.add(textos);

        // Derecha: 4 tarjetas de conteo por categoría con mini-arte de fondo
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        for (Categoria c : Categoria.values()) {
            TarjetaContador tarjeta = new TarjetaContador(c);
            tarjeta.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alternarFiltroPorTarjeta(c);
                }
            });
            tarjetas.put(c, tarjeta);
            derecha.add(tarjeta);
        }

        header.add(izquierda, BorderLayout.WEST);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    private void alternarFiltroPorTarjeta(Categoria categoria) {
        if (categoria.equals(cmbFiltroCategoria.getSelectedItem())) {
            cmbFiltroCategoria.setSelectedItem(FILTRO_TODAS);
        } else {
            cmbFiltroCategoria.setSelectedItem(categoria);
        }
    }

    private void conectarEventos() {
        servicio.agregarOyente(estado -> SwingUtilities.invokeLater(() ->
                indicadorEstado.actualizar(estado, servicio.cantidad(), servicio.getUltimaSincronizacion())));

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Optional<ItemArsenal> seleccionado = tabla.getItemSeleccionado();
                if (seleccionado.isPresent()) {
                    formulario.cargarParaEdicion(seleccionado.get());
                    dossierVisual.mostrarItem(seleccionado.get());
                } else {
                    formulario.limpiar(servicio.siguienteId());
                    dossierVisual.mostrarPreviewCategoria(formulario.getCategoriaSeleccionada());
                }
            }
        });

        formulario.setAlCambiarCategoria(cat -> {
            if (formulario.getIdEnEdicion().isEmpty()) {
                dossierVisual.mostrarPreviewCategoria(cat);
            }
        });

        formulario.getBtnAgregar().addActionListener(e -> crearRegistro());
        formulario.getBtnActualizar().addActionListener(e -> actualizarRegistro());
        formulario.getBtnEliminar().addActionListener(e -> eliminarRegistro());
        formulario.getBtnLimpiar().addActionListener(e -> limpiarSeleccion());

        Tema.alCambiar(txtBuscar.getDocument(), this::aplicarFiltros);
        cmbFiltroCategoria.addActionListener(e -> aplicarFiltros());
    }

    private void instalarAtajos() {
        JComponent root = getRootPane();

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "buscar");
        root.getActionMap().put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiar");
        root.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarSeleccion();
            }
        });

        tabla.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        tabla.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabla.getItemSeleccionado().isPresent()) {
                    eliminarRegistro();
                }
            }
        });
    }

    // ─────────────────────────── Operaciones CRUD ───────────────────────────

    private void crearRegistro() {
        PanelFormulario.DatosFormulario datos = formulario.validarYObtenerDatos();
        if (datos == null) {
            return;
        }
        try {
            ItemArsenal creado = servicio.crear(
                    datos.nombre(), datos.categoria(), datos.mapaOrigen(),
                    datos.rarezaONivel(), datos.packAPunch(), datos.descripcion());
            refrescarVista(creado.id());
            tabla.seleccionarPorId(creado.id());
            dossierVisual.mostrarItem(creado);
        } catch (IllegalArgumentException e) {
            formulario.mostrarError(e.getMessage());
        } catch (PersistenciaException e) {
            DialogoNeon.mostrar(this, "ERROR AL GUARDAR", e.getMessage(), Tema.SANGRE_TEXTO);
        }
    }

    private void actualizarRegistro() {
        var idOpt = formulario.getIdEnEdicion();
        if (idOpt.isEmpty()) {
            return;
        }
        PanelFormulario.DatosFormulario datos = formulario.validarYObtenerDatos();
        if (datos == null) {
            return;
        }
        try {
            ItemArsenal actualizado = servicio.actualizar(
                    idOpt.getAsInt(), datos.nombre(), datos.categoria(), datos.mapaOrigen(),
                    datos.rarezaONivel(), datos.packAPunch(), datos.descripcion());
            refrescarVista(actualizado.id());
            dossierVisual.mostrarItem(actualizado);
        } catch (IllegalArgumentException e) {
            formulario.mostrarError(e.getMessage());
        } catch (PersistenciaException e) {
            DialogoNeon.mostrar(this, "ERROR AL GUARDAR", e.getMessage(), Tema.SANGRE_TEXTO);
        }
    }

    private void eliminarRegistro() {
        var idOpt = formulario.getIdEnEdicion();
        if (idOpt.isEmpty()) {
            return;
        }
        int id = idOpt.getAsInt();
        Optional<ItemArsenal> existente = servicio.buscarPorId(id);
        if (existente.isEmpty()) {
            refrescarVista(null);
            return;
        }
        ItemArsenal item = existente.get();
        boolean confirmado = DialogoNeon.confirmar(
                this,
                "CONFIRMAR ELIMINACIÓN",
                "¿Eliminar «" + item.nombre() + "» (" + item.categoria().getEtiqueta()
                        + ") del arsenal?\n\nEsta acción actualizará el archivo local y no se puede deshacer.",
                Tema.SANGRE,
                "ELIMINAR");
        if (!confirmado) {
            return;
        }
        try {
            servicio.eliminar(id);
            refrescarVista(null);
            limpiarSeleccion();
        } catch (IllegalArgumentException e) {
            formulario.mostrarError(e.getMessage());
        } catch (PersistenciaException e) {
            DialogoNeon.mostrar(this, "ERROR AL GUARDAR", e.getMessage(), Tema.SANGRE_TEXTO);
        }
    }

    private void limpiarSeleccion() {
        tabla.clearSelection();
        formulario.limpiar(servicio.siguienteId());
        dossierVisual.mostrarPreviewCategoria(formulario.getCategoriaSeleccionada());
        formulario.enfocarNombre();
    }

    // ─────────────────────────── Refresco y filtros ───────────────────────────

    private void refrescarVista(Integer idASeleccionar) {
        modeloTabla.setItems(servicio.listarTodos());
        aplicarFiltros();
        actualizarContadores();
        if (idASeleccionar != null) {
            tabla.seleccionarPorId(idASeleccionar);
        } else if (tabla.getSelectedRow() < 0) {
            formulario.limpiar(servicio.siguienteId());
            dossierVisual.mostrarPreviewCategoria(formulario.getCategoriaSeleccionada());
        }
    }

    private void aplicarFiltros() {
        Object sel = cmbFiltroCategoria.getSelectedItem();
        Categoria cat = sel instanceof Categoria c ? c : null;
        tabla.aplicarFiltro(txtBuscar.getText(), cat);

        int visibles = tabla.getRowCount();
        int total = modeloTabla.getRowCount();
        lblResumenFiltro.setText(visibles == total
                ? total + " ítems"
                : visibles + " de " + total + " ítems");

        for (Map.Entry<Categoria, TarjetaContador> entry : tarjetas.entrySet()) {
            entry.getValue().setActiva(entry.getKey().equals(cat));
        }
    }

    private void actualizarContadores() {
        Map<Categoria, Long> conteo = servicio.contarPorCategoria();
        for (Map.Entry<Categoria, TarjetaContador> entry : tarjetas.entrySet()) {
            entry.getValue().setValor(conteo.getOrDefault(entry.getKey(), 0L));
        }
    }

    // ─────────────────────────── Subcomponentes del encabezado ───────────────────────────

    /** Logo oficial BO3 ("III") enmarcado en un badge redondeado con resplandor cálido. */
    private static final class LogoBO3Panel extends JPanel {
        LogoBO3Panel() {
            setOpaque(false);
            setPreferredSize(new Dimension(56, 56));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int r = 14;

            // Resplandor suave alrededor del logo
            g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 42));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, r, r);

            BufferedImage logo = Recursos.logoBO3();
            if (logo != null) {
                Recursos.pintarCover(g2, logo, 3, 3, w - 6, h - 6, r - 2, 1f);
            } else {
                g2.setColor(Tema.PALETA_NEGRO);
                g2.fillRoundRect(3, 3, w - 6, h - 6, r - 2, r - 2);
                g2.setFont(Tema.TITULO.deriveFont(18f));
                g2.setColor(Tema.PALETA_NARANJA);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("III", (w - fm.stringWidth("III")) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            }

            g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 165));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(3f, 3f, w - 6f, h - 6f, r - 2, r - 2));
            g2.dispose();
        }
    }

    /** Título bicromático con resplandor sutil (blanco platino + naranja BO3). */
    private static final class TituloBO3 extends JComponent {
        private final String parteBlanca;
        private final String parteNaranja;

        TituloBO3(String parteBlanca, String parteNaranja) {
            this.parteBlanca = parteBlanca;
            this.parteNaranja = parteNaranja;
            setFont(Tema.TITULO);
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(parteBlanca + parteNaranja) + 8, fm.getHeight() + 4);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = fm.getAscent() + 1;
            int wBlanca = fm.stringWidth(parteBlanca);

            // Sombra suave para contraste sobre el banner
            g2.setColor(Tema.alfa(Tema.PALETA_NEGRO, 180));
            g2.drawString(parteBlanca, 1, y + 2);
            g2.drawString(parteNaranja, wBlanca + 1, y + 2);

            // Parte blanca platino (#F4F4F4)
            g2.setColor(Tema.PALETA_BLANCO);
            g2.drawString(parteBlanca, 0, y);

            // Resplandor + parte naranja BO3 (#FF7400)
            g2.setColor(Tema.alfa(Tema.PALETA_NARANJA, 45));
            g2.drawString(parteNaranja, wBlanca + 1, y + 1);
            g2.setColor(Tema.PALETA_NARANJA);
            g2.drawString(parteNaranja, wBlanca, y);
            g2.dispose();
        }
    }

    /** Mini tarjeta de conteo por categoría con arte del juego difuminado de fondo. */
    private static final class TarjetaContador extends JPanel {
        private final Categoria categoria;
        private final Color color;
        private final JLabel lblNumero;
        private boolean hover;
        private boolean activa;

        TarjetaContador(Categoria categoria) {
            super(new BorderLayout(0, 0));
            this.categoria = categoria;
            this.color = Tema.colorCategoria(categoria);
            setOpaque(false);
            setPreferredSize(new Dimension(122, 56));
            setBorder(new EmptyBorder(7, 16, 7, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Filtrar por " + categoria.getPlural());

            lblNumero = new JLabel("0");
            lblNumero.setFont(Tema.NUMERO.deriveFont(18f));
            lblNumero.setForeground(Tema.PALETA_BLANCO);

            JLabel lblNombre = new JLabel(etiquetaCorta(categoria));
            lblNombre.setFont(Tema.ETIQUETA_MINI);
            lblNombre.setForeground(Tema.TEXTO_TENUE);

            add(lblNumero, BorderLayout.CENTER);
            add(lblNombre, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        private static String etiquetaCorta(Categoria c) {
            return switch (c) {
                case ARMA_MARAVILLA -> "MARAVILLA";
                case GOBBLEGUM -> "GOBBLEGUMS";
                case ARMA_ESPECIAL -> "ESPECIALES";
                case PERK_A_COLA -> "PERK-A-COLA";
            };
        }

        void setValor(long n) {
            lblNumero.setText(String.valueOf(n));
        }

        void setActiva(boolean activa) {
            this.activa = activa;
            lblNumero.setForeground(activa ? color : Tema.PALETA_BLANCO);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int r = Tema.RADIO_TARJETA;

            g2.setColor(Tema.PANEL);
            g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);

            // Mini-arte de la categoría suavemente visible al fondo
            BufferedImage arte = Recursos.arteCategoria(categoria);
            if (arte != null) {
                float op = activa ? 0.42f : hover ? 0.34f : 0.22f;
                Recursos.pintarCover(g2, arte, 0, 0, w - 1, h - 1, r, op);
                g2.setPaint(new GradientPaint(0, 0, Tema.alfa(Tema.PANEL, 210),
                        w, 0, Tema.alfa(Tema.PANEL, 95)));
                g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
            }

            if (activa) {
                g2.setColor(Tema.alfa(color, 26));
                g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
            }

            // Barra de acento redondeada
            g2.setColor(color);
            g2.fillRoundRect(4, 11, 3, h - 22, 3, 3);

            g2.setColor(activa ? color : hover ? Tema.mezclar(Tema.BORDE, color, 0.55) : Tema.BORDE);
            g2.setStroke(new BasicStroke(activa ? 1.4f : 1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
            g2.dispose();
        }
    }
}
