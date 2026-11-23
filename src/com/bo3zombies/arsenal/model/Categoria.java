package com.bo3zombies.arsenal.model;

import java.util.List;

/**
 * Categorías del arsenal de BO3 Zombies.
 * <p>
 * Los enum son serializables de forma nativa: en el archivo binario solo se
 * guarda el nombre de la constante (por ejemplo "GOBBLEGUM"), por lo que no
 * necesitan serialVersionUID.
 */
public enum Categoria {

    ARMA_MARAVILLA("Arma Maravilla", "Armas Maravilla",
            List.of("Común", "Rara", "Épica", "Legendaria", "Mítica")),

    GOBBLEGUM("GobbleGum", "GobbleGums",
            List.of("Clásica", "Mega", "Rara Mega", "Ultra Rara Mega", "Caprichosa")),

    ARMA_ESPECIAL("Arma Especial", "Armas Especiales",
            List.of("Nivel 1", "Nivel 2", "Nivel 3", "Mejorada", "Definitiva")),

    PERK_A_COLA("Perk-a-Cola", "Perk-a-Colas",
            List.of("Estándar", "Esencial", "Mejorada", "Edición Limitada"));

    private final String etiqueta;
    private final String plural;
    private final List<String> rarezasSugeridas;

    Categoria(String etiqueta, String plural, List<String> rarezasSugeridas) {
        this.etiqueta = etiqueta;
        this.plural = plural;
        this.rarezasSugeridas = rarezasSugeridas;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getPlural() {
        return plural;
    }

    /** Sugerencias de rareza / nivel que la interfaz muestra según la categoría. */
    public List<String> getRarezasSugeridas() {
        return rarezasSugeridas;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
