package org.data.definitions.candles;

import org.data.definitions.assets.Instrument;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.TimeFrame;

public interface CandleTimeSerie extends DoubleTimeSerie {


    Candle getCandle(int index);

    default Candle getFirst(){
        return getCandle(0);
    }
    default Candle getLast(){
        return getCandle(size() - 1);
    }

    DoubleTimeSerie getOpenTimeSerie();

    DoubleTimeSerie getCloseTimeSerie();

    DoubleTimeSerie getHighTimeSerie();

    DoubleTimeSerie getLowTimeSerie();

    DoubleTimeSerie getVolumeTimeSerie();

    Instrument getInstrument();

    TimeFrame getTimeFrame();

    @Override
    default double getValue(int index) {
        return getCloseTimeSerie().getValue(index);
    }

    @Override
    default int size() {
        return getCloseTimeSerie().size();
    }

    @Override
    default long getTimestamp(int index) {
        return getCloseTimeSerie().getTimestamp(index);
    }
}