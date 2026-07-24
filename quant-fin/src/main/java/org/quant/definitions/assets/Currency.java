package org.quant.definitions.assets;

import lombok.Getter;

@Getter
public enum Currency {
    EUR("Euro", "€"),
    USD("US Dollar", "$"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥");

    private final String name;
    private final String symbol;

    Currency(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

}