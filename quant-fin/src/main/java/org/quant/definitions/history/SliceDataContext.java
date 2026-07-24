package org.quant.definitions.history;

import org.quant.definitions.candles.CandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.assets.Purchasable;
import org.series.timeserie.TimeFrame;

public class SliceDataContext implements DataContext {

    public SliceDataContext(long start, long end, DataContext source){
    }


    @Override
    public CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame) {
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
