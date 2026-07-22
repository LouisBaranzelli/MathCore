package org.quant.definitions;

import lombok.Getter;
import org.math.vector.Vector;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.time.Instant;


@Getter
public class CompositeCandleTimeSerie implements CandleTimeSerie {

    private final Instrument instrument;
    private final TimeFrame timeframe;

    private final DoubleTimeSerie openTimeSerie;
    private final DoubleTimeSerie highTimeSerie;
    private final DoubleTimeSerie lowTimeSerie;
    private final DoubleTimeSerie closeTimeSerie;
    private final DoubleTimeSerie volumeTimeSerie;


    public CompositeCandleTimeSerie(Instrument instrument,
                                    TimeFrame timeframe,
                                    DoubleTimeSerie openTimeSerie,
                                    DoubleTimeSerie highTimeSerie,
                                    DoubleTimeSerie lowTimeSerie,
                                    DoubleTimeSerie closeTimeSerie,
                                    DoubleTimeSerie volumeTimeSerie) {

        if (openTimeSerie == null || highTimeSerie == null ||
                lowTimeSerie == null || closeTimeSerie == null || volumeTimeSerie == null) {
            throw new IllegalArgumentException("Input time series cannot be null.");
        }

        int size = closeTimeSerie.size();
        if (openTimeSerie.size() != size || highTimeSerie.size() != size ||
                lowTimeSerie.size() != size || volumeTimeSerie.size() != size) {
            throw new IllegalArgumentException("All input DoubleTimeSeries must have strictly equal sizes.");
        }

        for (int i = 0; i < size; i++) {
            long targetTimestamp = closeTimeSerie.getTimestamp(i);

            if (openTimeSerie.getTimestamp(i) != targetTimestamp ||
                    highTimeSerie.getTimestamp(i) != targetTimestamp ||
                    lowTimeSerie.getTimestamp(i) != targetTimestamp ||
                    volumeTimeSerie.getTimestamp(i) != targetTimestamp) {

                throw new IllegalArgumentException(String.format(
                        "Timestamp misalignment detected at index %d. Expected timestamp (Close): %d",
                        i, targetTimestamp
                ));
            }
        }

        this.instrument = instrument;
        this.timeframe = timeframe;
        this.openTimeSerie = openTimeSerie;
        this.highTimeSerie = highTimeSerie;
        this.lowTimeSerie = lowTimeSerie;
        this.closeTimeSerie = closeTimeSerie;
        this.volumeTimeSerie = volumeTimeSerie;
    }

    @Override
    public Candle getCandle(int index) {
        return new Candle(
                instrument,
                Instant.ofEpochMilli(getTimestamp(index)),
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