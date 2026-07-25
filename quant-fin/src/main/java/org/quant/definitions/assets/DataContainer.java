package org.quant.definitions.assets;

import lombok.Getter;
import org.quant.definitions.candles.CandleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class DataContainer {

    @Getter
    private final Instrument instrument;

    private final Map<TimeFrame, CandleTimeSerie> data = new EnumMap<>(TimeFrame.class);

    public DataContainer(Instrument instrument){
        this.instrument = instrument;
    }

    public void addData(TimeFrame timeFrame, CandleTimeSerie candleTimeSerie){
        if (data.containsKey(timeFrame)){
            throw new IllegalArgumentException(String.format("timeframe %s already existing in this container", timeFrame.getLabel()));
        }
        data.put(timeFrame, candleTimeSerie);
    }


    public CandleTimeSerie getCandleTimeSerie(TimeFrame timeFrame){
        Objects.requireNonNull(timeFrame, "timeFrame cannot be null");
        CandleTimeSerie candleTimeSerie = data.get(timeFrame);
        if (candleTimeSerie == null){
            throw new IllegalArgumentException(String.format("missing data for %s for time frame %s", instrument.getLabel(), timeFrame.getLabel()));
        } else {
            return candleTimeSerie;
        }
    }



}
