package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.RandomCandleTimeSerie;
import org.series.InvalidTimeSerieException;
import org.series.timeserie.TimeFrame;

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
