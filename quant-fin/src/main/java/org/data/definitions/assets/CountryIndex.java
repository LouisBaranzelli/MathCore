package org.data.definitions.assets;

import lombok.Getter;
import org.series.ZoneIdEnum;

@Getter
public enum CountryIndex implements GeographicInstrument {

    CAC40("PX1", "CAC 40", Country.FR, Currency.EUR, ZoneIdEnum.EUROPE_PARIS);

    private final String ticker;
    private final String label;
    private final Country country;
    private final Currency currency;
    private final ZoneIdEnum zoneIdEnum;



    CountryIndex(String ticker, String label, Country country, Currency currency, ZoneIdEnum zoneIdEnum) {
        this.ticker = ticker;
        this.label = label;
        this.country = country;
        this.currency = currency;
        this.zoneIdEnum = zoneIdEnum;
    }
}
