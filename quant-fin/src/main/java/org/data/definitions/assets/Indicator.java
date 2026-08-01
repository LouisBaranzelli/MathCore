package org.data.definitions.assets;

import lombok.Getter;
import org.series.ZoneIdEnum;

@Getter
public enum Indicator implements Instrument {

    VIX("VIX", "CBOE Volatility Index", ZoneIdEnum.AMERICA_NEW_YORK),
    DXY("DXY", "US Dollar Index", ZoneIdEnum.AMERICA_NEW_YORK);

    private final String ticker;
    private final String label;
    private final ZoneIdEnum zoneIdEnum;

    Indicator(String ticker, String label, ZoneIdEnum zoneIdEnum) {
        this.ticker = ticker;
        this.label = label;
        this.zoneIdEnum = zoneIdEnum;
    }

}