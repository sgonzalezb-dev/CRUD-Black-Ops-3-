package com.bo3zombies.arsenal;

import com.bo3zombies.arsenal.model.ItemArsenal;
import com.bo3zombies.arsenal.persistence.ArsenalRepository;
import com.bo3zombies.arsenal.service.ArsenalService;
import com.bo3zombies.arsenal.ui.DialogoNeon;
import com.bo3zombies.arsenal.ui.Tema;
import com.bo3zombies.arsenal.ui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Punto de entrada: BO3 Zombies · Arsenal del Elemento 115. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        Tema.instalarDefaults();

        ArsenalRepository repositorio = new ArsenalRepository();
        List<ItemArsenal> items;
        String aviso = null;

        try {
            items = repositorio.cargar(); // si el archivo no existe, devuelve lista vacía
        } catch (IOException e) {
            items = new ArrayList<>();
            try {
                Path respaldo = repositorio.respaldarArchivoDanado();
                aviso = "No se pudo leer el archivo de datos: " + e.getMessage() + ".\n\n"
                        + "Se renombró a «" + respaldo.getFileName() + "» para no perderlo "
                        + "y la aplicación arrancó con el arsenal vacío.";
            } catch (IOException ex) {
                aviso = "No se pudo leer ni renombrar el archivo de datos: " + ex.getMessage() + ".\n\n"
                        + "Si guardas cambios, el archivo se sobrescribirá.";
            }
        }

        ArsenalService servicio = new ArsenalService(repositorio, items);
        VentanaPrincipal ventana = new VentanaPrincipal(servicio);
        ventana.setVisible(true);

        if (aviso != null) {
            String mensaje = aviso;
            SwingUtilities.invokeLater(() ->
                    DialogoNeon.mostrar(ventana, "ARCHIVO DE DATOS DAÑADO", mensaje, Tema.AMBAR));
        }
    }
}
