package org.quant.definitions;

import lombok.Getter;

@Getter
public enum Stock implements GeographicInstrument, FieldInstrument{
    SU("SU.PA", "Schneider Electric", Field.ELECTRICAL_EQUIPMENT, Country.FR, Currency.EUR);

    private final String ticker;
    private final String label;
    private final Field field;
    private final Country country;
    private final Currency currency;


    Stock(String ticker, String label, Field field, Country country, Currency currency) {
        this.ticker = ticker;
        this.country = country;
        this.field = field;
        this.label = label;
        this.currency = currency;
    }
}
