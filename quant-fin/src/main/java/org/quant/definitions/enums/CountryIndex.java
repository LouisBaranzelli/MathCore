package org.quant.definitions.enums;

import lombok.Getter;

@Getter
public enum CountryIndex implements GeographicInstrument {

    CAC40("PX1", "CAC 40", Country.FR, Currency.EUR);

    private final String ticker;
    private final String label;
    private final Country country;
    private final Currency currency;


    CountryIndex(String ticker, String label, Country country, Currency currency) {
        this.ticker = ticker;
        this.label = label;
        this.country = country;
        this.currency = currency;
    }
}
