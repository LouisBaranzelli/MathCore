package org.quant.definitions.candles;

import org.series.timeserie.DoubleTimeSerie;

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