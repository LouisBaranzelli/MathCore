package org.data.definitions.assets;

import lombok.Getter;
import org.series.ZoneIdEnum;

@Getter
public enum FieldIndex implements FieldInstrument {

    CAC40("PX1", "CAC 40", Field.TEXTILES_APPAREL_LUXURY_GOODS, Currency.EUR, ZoneIdEnum.EUROPE_PARIS);

    private final String ticker;
    private final String label;
    private final Field field;
    private final Currency currency;
    private final ZoneIdEnum zoneIdEnum;

    FieldIndex(String ticker, String label, Field field, Currency currency, ZoneIdEnum zoneIdEnum) {
        this.ticker = ticker;
        this.label = label;
        this.field = field;
        this.currency = currency;
        this.zoneIdEnum = zoneIdEnum;
    }
}