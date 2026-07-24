package org.quant.definitions.assets;

import lombok.Getter;

@Getter
public enum FieldIndex implements FieldInstrument {

    CAC40("PX1", "CAC 40", Field.TEXTILES_APPAREL_LUXURY_GOODS, Currency.EUR);

    private final String ticker;
    private final String label;
    private final Field field;
    private final Currency currency;

    FieldIndex(String ticker, String label, Field field, Currency currency) {
        this.ticker = ticker;
        this.label = label;
        this.field = field;
        this.currency = currency;
    }
}