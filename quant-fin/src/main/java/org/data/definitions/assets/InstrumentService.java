package org.data.definitions.assets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InstrumentService {

    /**
     * Recherche les instruments selon un secteur (Field) et/ou un Pays (Country).
     * Les filtres null sont ignorés.
     *
     * @param field   Le secteur d'activité (null pour ignorer)
     * @param country Le pays (null pour ignorer)
     * @return La liste des instruments correspondants.
     */
    public static List<Instrument> findInstruments(Field field, Country country) {
        return Arrays.stream(Stock.values())
                .filter(stock -> field == null || stock.getField() == field)
                .filter(stock -> country == null || stock.getCountry() == country)
                .map(stock -> (Instrument) stock)
                .toList();
    }

    /**
     * Recherche par secteur uniquement.
     */
    public static List<Instrument> findByField(Field field) {
        Objects.requireNonNull(field, "Field cannot be null");
        return findInstruments(field, null);
    }

    /**
     * Recherche par pays uniquement.
     */
    public static List<Instrument> findByCountry(Country country) {
        Objects.requireNonNull(country, "Country cannot be null");
        return findInstruments(null, country);
    }
}
