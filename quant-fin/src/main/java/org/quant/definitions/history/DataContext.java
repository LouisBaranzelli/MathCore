package org.quant.definitions.history;

import org.quant.definitions.candles.CandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.assets.Purchasable;
import org.series.timeserie.TimeFrame;

import java.util.Set;

public interface DataContext {

    CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame);

    public long getCurrentTimestamp();

    public Set<Instrument> getInstruments();

    public String getDescription();
}
