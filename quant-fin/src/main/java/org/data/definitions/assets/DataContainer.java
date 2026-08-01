package org.data.definitions.assets;

import lombok.Getter;
import org.data.definitions.TickEnum;
import org.data.definitions.TickService;
import org.data.definitions.candles.Candle;
import org.data.definitions.candles.CandleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataContainer {

    @Getter
    private final Instrument instrument;

    @Getter
    private final Map<TimeFrame, CandleTimeSerie> data = new EnumMap<>(TimeFrame.class);

    private final HashMap<Long, Candle> availableCandlesTickSecond;
    private final HashMap<Long, Candle> availableCandlesTickDay;

    public DataContainer(Instrument instrument){

        this.instrument = instrument;
        this.availableCandlesTickSecond = new HashMap<>();
        this.availableCandlesTickDay = new HashMap<>();
    }

    public void addData(CandleTimeSerie candleTimeSerie){
        if (data.containsKey(candleTimeSerie.getTimeFrame())){
            throw new IllegalArgumentException(String.format("timeframe %s already existing in this container", candleTimeSerie.getTimeFrame()));
        }
        HashMap<Long, Candle> relevantDict = getRelevantDict(candleTimeSerie.getTimeFrame());
        data.put(candleTimeSerie.getTimeFrame(), candleTimeSerie);
        IntStream.range(0, candleTimeSerie.size())
                .mapToObj(candleTimeSerie::getCandle)
                .forEach(candle -> relevantDict.putIfAbsent(candle.timestamp(), candle));
    }

    private HashMap<Long, Candle> getRelevantDict(TimeFrame timeFrame) {
        if (TickService.getTick(timeFrame) == TickEnum.DAY){
            return availableCandlesTickDay;
        }
        if (TickService.getTick(timeFrame) == TickEnum.SECOND){
            return availableCandlesTickSecond;
        }
        return null;
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

    public Set<Candle> getCandles(){
        Map<TimeFrame, CandleTimeSerie> candleTimeSerieMap = getData();
        return candleTimeSerieMap.values().stream()
                .flatMap(serie -> IntStream.range(0, serie.size())
                        .mapToObj(serie::getCandle))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Candle::timestamp))));
    }

    Candle getCandle(Long time, TimeFrame timeFrame){
        return getRelevantDict(timeFrame).get(time);
    }



}
