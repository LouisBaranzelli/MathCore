package org.data.definitions.history;

import lombok.Getter;
import org.common.TriConsumer;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DummyDataLoader implements DataLoader {

    @Getter
    private final TriConsumer<Instrument, TimeFrame, List<Candle>>  triConsumerOnSuccessLoading = null;

    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        List<Candle> output = new ArrayList<>();
        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame, instrument.getZoneIdEnum().getZoneId());
        IntStream.range(0, size).forEach(i -> output.add(Candle.randomCandle(i * TimeTools.fromDurationToLong(timeFrame.getDuration()) +  start)));
        return output;
    }

    @Override
    public String getLabel() {
        return "dummy Dataloader";
    }

    @Override
    public void onSuccessLoading(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) {

    }
}
