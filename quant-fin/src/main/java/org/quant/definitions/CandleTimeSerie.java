package org.quant.definitions;

import org.series.timeserie.DoubleTimeSerie;

public interface CandleTimeSerie extends DoubleTimeSerie {


    Candle getCandle(int index);

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