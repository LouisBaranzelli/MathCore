package org.quant.definitions;

import lombok.Getter;

@Getter
public enum Indicator implements Instrument {

    VIX("VIX", "CBOE Volatility Index"),
    DXY("DXY", "US Dollar Index");

    private final String ticker;
    private final String label;

    Indicator(String ticker, String label) {
        this.ticker = ticker;
        this.label = label;
    }
}