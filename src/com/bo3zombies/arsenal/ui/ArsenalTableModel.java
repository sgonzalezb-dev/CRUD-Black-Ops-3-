package com.bo3zombies.arsenal.ui;

import com.bo3zombies.arsenal.model.Categoria;
import com.bo3zombies.arsenal.model.ItemArsenal;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Adaptador entre {@code List<ItemArsenal>} y {@link javax.swing.JTable}. */
public class ArsenalTableModel extends AbstractTableModel {

    public static final int COL_ID = 0;
    public static final int COL_NOMBRE = 1;
    public static final int COL_CATEGORIA = 2;
    public static final int COL_MAPA = 3;
    public static final int COL_RAREZA = 4;
    public static final int COL_PAP = 5;
    public static final int COL_DESCRIPCION = 6;

    private static final String[] COLUMNAS = {
            "ID", "NOMBRE", "CATEGORÍA", "MAPA DE ORIGEN", "RAREZA", "PACK-A-PUNCH", "DESCRIPCIÓN"
    };

    private List<ItemArsenal> filas = new ArrayList<>();

    public void setItems(List<ItemArsenal> items) {
        this.filas = new ArrayList<>(items);
        fireTableDataChanged();
    }

    public ItemArsenal getItemEn(int filaModelo) {
        return filas.get(filaModelo);
    }

    public int indicePorId(int id) {
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).id() == id) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNAS[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case COL_ID -> Integer.class;
            case COL_CATEGORIA -> Categoria.class;
            case COL_PAP -> Boolean.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int fila, int col) {
        ItemArsenal item = filas.get(fila);
        return switch (col) {
            case COL_ID -> item.id();
            case COL_NOMBRE -> item.nombre();
            case COL_CATEGORIA -> item.categoria();
            case COL_MAPA -> item.mapaOrigen();
            case COL_RAREZA -> item.rarezaONivel();
            case COL_PAP -> item.packAPunch();
            case COL_DESCRIPCION -> item.descripcion();
            default -> "";
        };
    }
}
