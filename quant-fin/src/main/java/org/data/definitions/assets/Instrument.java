package org.data.definitions.assets;

import org.series.ZoneIdEnum;

public interface Instrument {
    String getTicker();
    String getLabel();
    ZoneIdEnum getZoneIdEnum();
}
