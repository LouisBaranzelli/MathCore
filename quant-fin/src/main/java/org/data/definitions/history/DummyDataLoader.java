package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.assets.Stock;
import org.data.definitions.candles.Candle;
import org.data.definitions.candles.RandomCandleTimeSerie;
import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DummyDataLoader implements DataLoader {
    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        List<Candle> output = new ArrayList<>();
        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame);
        IntStream.range(0, size).forEach(i -> output.add(Candle.randomCandle(i * TimeTools.fromDurationToLong(timeFrame.getDuration()) +  start)));
        return output;
    }

    @Override
    public String getLabel() {
        return "dummy Dataloader";
    }
}
