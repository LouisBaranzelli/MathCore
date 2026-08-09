package org.data.csv;

import org.data.SavingException;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.series.timeserie.TimeFrame;

import java.util.List;

public interface DataSaver {

    public void save(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) throws SavingException;

    public String getLabel();
}
