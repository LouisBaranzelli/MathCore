package org.quant.definitions.history;

import org.quant.definitions.candles.CompositeCandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.assets.Purchasable;
import org.series.timeserie.TimeFrame;

public class FullDataContext implements DataContext {

    public FullDataContext(long start, long end, Instrument... instrument){
    }

    @Override
    public CompositeCandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame) {
        return null;
    }

    @Override
    public long getCurrentTimestamp() {
        return 0;
    }

    @Override
    public Purchasable[] getPurchasables() {
        return new Purchasable[0];
    }
}
