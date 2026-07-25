package org.quant.definitions.history;

import org.quant.definitions.LoadingException;
import org.quant.definitions.assets.DataContainer;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.candles.RandomCandleTimeSerie;
import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.timegrid.TimeGrid;
import org.series.timegrid.TimeGridFactory;
import org.series.timeserie.RandomDoubleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class DummyDataloader implements Dataloader{
    @Override
    public DataContainer load(long start, long end, Instrument instrument, TimeFrame... timeFrame) throws LoadingException {
        DataContainer dataContainer = new DataContainer(instrument);
        Arrays.stream(timeFrame).forEach(t -> {
            try {
                dataContainer.addData(t, new RandomCandleTimeSerie(instrument, t, start, end));
            } catch (InvalidTimeSerieException e) {
                throw new RuntimeException(e);
            }
        });
        return dataContainer;
    }

    @Override
    public String getLabel() {
        return "dummy Dataloader";
    }
}
