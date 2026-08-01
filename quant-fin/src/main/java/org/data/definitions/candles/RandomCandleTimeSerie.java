package org.data.definitions.candles;

import lombok.Getter;
import org.math.vector.Vector;
import org.data.definitions.assets.Instrument;
import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.timegrid.TimeGrid;
import org.series.timegrid.TimeGridFactory;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.RandomDoubleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.time.ZonedDateTime;

@Getter
public class RandomCandleTimeSerie implements CandleTimeSerie{

    private final Instrument instrument;
    private final TimeFrame timeFrame;

    private final DoubleTimeSerie openTimeSerie;
    private final DoubleTimeSerie highTimeSerie;
    private final DoubleTimeSerie lowTimeSerie;
    private final DoubleTimeSerie closeTimeSerie;
    private final DoubleTimeSerie volumeTimeSerie;



    public RandomCandleTimeSerie(Instrument instrument,
                                 TimeFrame timeFrame, long start, long end) throws InvalidTimeSerieException {


        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame);
        TimeGrid timeGrid = TimeGridFactory.create(
                end,
                size,
                ( z) -> true,
                timeFrame
        );


        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.openTimeSerie = new RandomDoubleTimeSerie(timeGrid, 1, 0.5);
        this.highTimeSerie = new RandomDoubleTimeSerie(timeGrid, 1, 0.5);
        this.lowTimeSerie = new RandomDoubleTimeSerie(timeGrid, 1, 0.5);
        this.closeTimeSerie = new RandomDoubleTimeSerie(timeGrid, 1, 0.5);
        this.volumeTimeSerie = new RandomDoubleTimeSerie(timeGrid, 1, 0.5);

    }

    @Override
    public Candle getCandle(int index) {
        return new Candle(
                instrument,
                getTimestamp(index),
                openTimeSerie.getValue(index),
                highTimeSerie.getValue(index),
                lowTimeSerie.getValue(index),
                closeTimeSerie.getValue(index),
                volumeTimeSerie.getValue(index)
        );
    }

    @Override
    public Vector toVector() {
        return closeTimeSerie.toVector();
    }
}
