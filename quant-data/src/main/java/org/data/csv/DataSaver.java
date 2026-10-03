package org.data.csv;

import org.data.SavingException;
import org.fin.definitions.assets.Instrument;
import org.fin.definitions.candles.Candle;
import org.series.timeserie.TimeFrame;

import java.util.List;

public interface DataSaver {

    public void save(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) throws SavingException;

    public String getLabel();
}
