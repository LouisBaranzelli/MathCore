package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.series.timeserie.TimeFrame;

import java.util.List;

public interface DataLoader {

    List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException;

    String getLabel();
}
