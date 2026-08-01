package org.data.csv;

import org.data.definitions.LoadingException;
import org.data.definitions.TickEnum;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.series.timeserie.TimeFrame;

import java.io.IOException;
import java.util.List;

public interface DataSaver {

    public void save(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) throws IOException, LoadingException;

}
