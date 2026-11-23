package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;
import com.bo3zombies.arsenal.model.Mapas;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.OptionalInt;

/**
 * Formulario lateral para Crear y Actualizar registros del arsenal.
 * Cambia visualmente entre modo "NUEVO REGISTRO" (cian) y "EDITANDO #ID" (ámbar).
 */
public class PanelFormulario extends JPanel {

    /** Datos listos para enviarse al servicio. */
    public record DatosFormulario(
            String nombre,
            Categoria categoria,
            String mapaOrigen,
            String rarezaONivel,
            boolean packAPunch,
            String descripcion
    ) {
    }

    private final JLabel lblModo;
    private final JLabel lblIdBadge;
    private final CampoTexto txtNombre;
    private final ComboOscuro<Categoria> cmbCategoria;
    private final ComboOscuro<String> cmbMapa;
    private final ComboOscuro<String> cmbRareza;
    private final SwitchPackAPunch chkPackAPunch;
    private final AreaTexto txtDescripcion;
    private final JLabel lblContadorDesc;
    private final JLabel lblMensajeError;

    private final BotonNeon btnAgregar;
    private final BotonNeon btnActualizar;
    private final BotonNeon btnEliminar;
    private final BotonNeon btnLimpiar;

    private Integer idEnEdicion;
    private boolean actualizandoCombos;
    private java.util.function.Consumer<Categoria> oyenteCategoria;

    public PanelFormulario() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setPreferredSize(new Dimension(370, 0));
        setMinimumSize(new Dimension(340, 0));
        setBorder(new EmptyBorder(18, 18, 18, 18));

        // ───────── Cabecera del formulario ─────────
        JPanel cabecera = new JPanel(new BorderLayout(8, 0));
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.SEPARADOR),
                new EmptyBorder(0, 0, 12, 0)));

        lblModo = new JLabel("NUEVO REGISTRO");
        lblModo.setFont(Tema.SECCION);
        lblModo.setForeground(Tema.NEON);
        lblModo.setIcon(new IconoPunto(Tema.NEON, 9));
        lblModo.setIconTextGap(9);

        lblIdBadge = new JLabel("AUTO");
        lblIdBadge.setFont(Tema.MONO);
        lblIdBadge.setForeground(Tema.TEXTO_TENUE);
        lblIdBadge.setOpaque(true);
        lblIdBadge.setBackground(Tema.PANEL_ALT);
        lblIdBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE, 1),
                new EmptyBorder(3, 8, 3, 8)));

        cabecera.add(lblModo, BorderLayout.WEST);
        cabecera.add(lblIdBadge, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        // ───────── Campos ─────────
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Nombre
        fila = agregarEtiqueta(campos, gbc, fila, "NOMBRE DEL EQUIPAMIENTO *");
        txtNombre = new CampoTexto("Ej. Ragnarok DG-4, Perkaholic, Juggernog…");
        txtNombre.setLimite(ItemArsenal.MAX_NOMBRE);
        fila = agregarCampo(campos, gbc, fila, txtNombre);

        // Categoría
        fila = agregarEtiqueta(campos, gbc, fila, "CATEGORÍA *");
        cmbCategoria = new ComboOscuro<>(Categoria.values());
        cmbCategoria.addActionListener(e -> {
            if (!actualizandoCombos) {
                Categoria sel = (Categoria) cmbCategoria.getSelectedItem();
                cargarSugerenciasRareza(sel, false);
                if (oyenteCategoria != null && sel != null) {
                    oyenteCategoria.accept(sel);
                }
            }
        });
        fila = agregarCampo(campos, gbc, fila, cmbCategoria);

        // Mapa de origen
        fila = agregarEtiqueta(campos, gbc, fila, "MAPA DE ORIGEN *");
        cmbMapa = new ComboOscuro<>(Mapas.TODOS.toArray(String[]::new));
        cmbMapa.hacerEditable(ItemArsenal.MAX_TEXTO_CORTO);
        fila = agregarCampo(campos, gbc, fila, cmbMapa);

        // Rareza / Nivel
        fila = agregarEtiqueta(campos, gbc, fila, "RAREZA O NIVEL *");
        cmbRareza = new ComboOscuro<>();
        cmbRareza.hacerEditable(ItemArsenal.MAX_TEXTO_CORTO);
        fila = agregarCampo(campos, gbc, fila, cmbRareza);

        // Pack-a-Punch
        chkPackAPunch = new SwitchPackAPunch("Mejorado con Pack-a-Punch");
        gbc.gridy = fila++;
        gbc.insets = new Insets(10, 0, 6, 0);
        campos.add(chkPackAPunch, gbc);

        // Descripción + contador
        JPanel filaDescHeader = new JPanel(new BorderLayout());
        filaDescHeader.setOpaque(false);
        JLabel lblDesc = new JLabel("DESCRIPCIÓN / EFECTO");
        lblDesc.setFont(Tema.ETIQUETA);
        lblDesc.setForeground(Tema.TEXTO_TENUE);
        lblContadorDesc = new JLabel("0 / " + ItemArsenal.MAX_DESCRIPCION);
        lblContadorDesc.setFont(Tema.MONO.deriveFont(11f));
        lblContadorDesc.setForeground(Tema.TEXTO_APAGADO);
        filaDescHeader.add(lblDesc, BorderLayout.WEST);
        filaDescHeader.add(lblContadorDesc, BorderLayout.EAST);

        gbc.gridy = fila++;
        gbc.insets = new Insets(6, 0, 4, 0);
        campos.add(filaDescHeader, gbc);

        txtDescripcion = new AreaTexto(4);
        txtDescripcion.setLimite(ItemArsenal.MAX_DESCRIPCION);
        txtDescripcion.setPreferredSize(new Dimension(0, 96));
        Tema.alCambiar(txtDescripcion.getDocument(), this::actualizarContadorDesc);

        gbc.gridy = fila++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 6, 0);
        campos.add(txtDescripcion, gbc);

        // Banner de error en línea (sin popups molestos)
        lblMensajeError = new JLabel(" ");
        lblMensajeError.setFont(Tema.PEQUENA_NEGRITA);
        lblMensajeError.setForeground(Tema.SANGRE_TEXTO);
        lblMensajeError.setBorder(new EmptyBorder(2, 2, 2, 2));
        gbc.gridy = fila;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        campos.add(lblMensajeError, gbc);

        add(campos, BorderLayout.CENTER);

        // ───────── Botones CRUD ─────────
        JPanel botones = new JPanel(new GridLayout(2, 2, 10, 10));
        botones.setOpaque(false);
        botones.setBorder(new EmptyBorder(6, 0, 0, 0));

        btnAgregar = new BotonNeon("AGREGAR", Tema.NEON, true);
        btnActualizar = new BotonNeon("ACTUALIZAR", Tema.AMBAR);
        btnEliminar = new BotonNeon("ELIMINAR", Tema.SANGRE);
        btnLimpiar = new BotonNeon("LIMPIAR", Tema.AMBAR);

        botones.add(btnAgregar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        add(botones, BorderLayout.SOUTH);

        limpiar(1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g);
        Color borde = idEnEdicion != null
                ? Tema.mezclar(Tema.BORDE, Tema.AMBAR, 0.55)
                : Tema.BORDE;
        Tema.pintarSuperficieSuave(g2, getWidth(), getHeight(), Tema.RADIO_PANEL, borde);
        g2.dispose();
    }

    public void setAlCambiarCategoria(java.util.function.Consumer<Categoria> oyente) {
        this.oyenteCategoria = oyente;
    }

    public Categoria getCategoriaSeleccionada() {
        return (Categoria) cmbCategoria.getSelectedItem();
    }

    private int agregarEtiqueta(JPanel panel, GridBagConstraints gbc, int fila, String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(Tema.ETIQUETA);
        lbl.setForeground(Tema.TEXTO_TENUE);
        gbc.gridy = fila;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(fila == 0 ? 0 : 8, 0, 4, 0);
        panel.add(lbl, gbc);
        return fila + 1;
    }

    private int agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, java.awt.Component comp) {
        gbc.gridy = fila;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 2, 0);
        panel.add(comp, gbc);
        return fila + 1;
    }

    private void cargarSugerenciasRareza(Categoria categoria, boolean preservarTexto) {
        if (categoria == null) {
            return;
        }
        String previo = cmbRareza.getTextoEditor();
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>(
                categoria.getRarezasSugeridas().toArray(String[]::new));
        cmbRareza.setModel(modelo);
        if (preservarTexto && !previo.isBlank()) {
            cmbRareza.setTextoEditor(previo);
        } else if (modelo.getSize() > 0) {
            cmbRareza.setSelectedIndex(0);
        }
    }

    private void actualizarContadorDesc() {
        int len = txtDescripcion.getText().length();
        lblContadorDesc.setText(len + " / " + ItemArsenal.MAX_DESCRIPCION);
        lblContadorDesc.setForeground(len >= ItemArsenal.MAX_DESCRIPCION - 30
                ? Tema.AMBAR : Tema.TEXTO_APAGADO);
    }

    /** Limpia el formulario y lo coloca en modo "NUEVO REGISTRO". */
    public void limpiar(int proximoId) {
        idEnEdicion = null;
        actualizandoCombos = true;
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        cmbMapa.setSelectedIndex(0);
        cmbMapa.setTextoEditor(Mapas.TODOS.get(0));
        cargarSugerenciasRareza(Categoria.ARMA_MARAVILLA, false);
        chkPackAPunch.setSelected(false);
        txtDescripcion.setText("");
        actualizandoCombos = false;

        limpiarErrores();
        lblModo.setText("NUEVO REGISTRO");
        lblModo.setForeground(Tema.NEON);
        lblModo.setIcon(new IconoPunto(Tema.NEON, 9));
        lblIdBadge.setText(String.format("#%03d AUTO", proximoId));
        lblIdBadge.setForeground(Tema.NEON);

        btnAgregar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        repaint();
    }

    /** Carga un registro existente en el formulario y activa el modo edición. */
    public void cargarParaEdicion(ItemArsenal item) {
        idEnEdicion = item.id();
        actualizandoCombos = true;
        txtNombre.setText(item.nombre());
        cmbCategoria.setSelectedItem(item.categoria());
        cargarSugerenciasRareza(item.categoria(), false);
        cmbMapa.setTextoEditor(item.mapaOrigen());
        cmbRareza.setTextoEditor(item.rarezaONivel());
        chkPackAPunch.setSelected(item.packAPunch());
        txtDescripcion.setText(item.descripcion());
        actualizandoCombos = false;

        limpiarErrores();
        lblModo.setText(String.format("EDITANDO #%03d", item.id()));
        lblModo.setForeground(Tema.AMBAR);
        lblModo.setIcon(new IconoPunto(Tema.AMBAR, 9));
        lblIdBadge.setText(String.format("ID #%03d", item.id()));
        lblIdBadge.setForeground(Tema.AMBAR);

        btnAgregar.setEnabled(true);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
        repaint();
    }

    /** Valida los campos del formulario; si algo falta, marca el campo en rojo y devuelve null. */
    public DatosFormulario validarYObtenerDatos() {
        limpiarErrores();

        String nombre = txtNombre.getText().strip();
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        String mapa = cmbMapa.getTextoEditor().strip();
        String rareza = cmbRareza.getTextoEditor().strip();
        boolean pap = chkPackAPunch.isSelected();
        String desc = txtDescripcion.getText().strip();

        if (nombre.isEmpty()) {
            txtNombre.setError(true);
            txtNombre.requestFocusInWindow();
            mostrarError("El nombre del equipamiento es obligatorio.");
            return null;
        }
        if (categoria == null) {
            cmbCategoria.setError(true);
            cmbCategoria.requestFocusInWindow();
            mostrarError("Selecciona una categoría válida.");
            return null;
        }
        if (mapa.isEmpty()) {
            cmbMapa.setError(true);
            cmbMapa.requestFocusInWindow();
            mostrarError("Indica el mapa de origen.");
            return null;
        }
        if (rareza.isEmpty()) {
            cmbRareza.setError(true);
            cmbRareza.requestFocusInWindow();
            mostrarError("Indica la rareza o nivel.");
            return null;
        }
        return new DatosFormulario(nombre, categoria, mapa, rareza, pap, desc);
    }

    public void mostrarError(String mensaje) {
        lblMensajeError.setText(Tema.html("⚠ " + mensaje, 320));
    }

    public void limpiarErrores() {
        txtNombre.setError(false);
        cmbCategoria.setError(false);
        cmbMapa.setError(false);
        cmbRareza.setError(false);
        txtDescripcion.setError(false);
        lblMensajeError.setText(" ");
    }

    public OptionalInt getIdEnEdicion() {
        return idEnEdicion == null ? OptionalInt.empty() : OptionalInt.of(idEnEdicion);
    }

    public void enfocarNombre() {
        txtNombre.requestFocusInWindow();
    }

    public BotonNeon getBtnAgregar() {
        return btnAgregar;
    }

    public BotonNeon getBtnActualizar() {
        return btnActualizar;
    }

    public BotonNeon getBtnEliminar() {
        return btnEliminar;
    }

    public BotonNeon getBtnLimpiar() {
        return btnLimpiar;
    }
}
