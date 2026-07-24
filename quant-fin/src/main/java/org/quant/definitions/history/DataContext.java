package org.quant.definitions.history;

import org.quant.definitions.candles.CandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.assets.Purchasable;
import org.series.timeserie.TimeFrame;

public interface DataContext {
    CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame);

    public long getCurrentTimestamp();

    public Purchasable[] getPurchasables();

}
