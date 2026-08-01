package org.data.definitions.assets;

import org.data.definitions.candles.CompositeCandleTimeSerie;
import org.data.definitions.candles.RandomCandleTimeSerie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.ImmutableDoubleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataContainerTest {

    private CompositeCandleTimeSerie sourceSerie;
    private final long[] timestamps = {1000L, 1000L, 3000L, 4000L, 4000L};

    @BeforeEach
    void setUp() {
        DoubleTimeSerie open = createSerie(timestamps, new double[]{10.0, 20.0, 30.0, 40.0, 50.0});
        DoubleTimeSerie high = createSerie(timestamps, new double[]{15.0, 25.0, 35.0, 45.0, 55.0});
        DoubleTimeSerie low = createSerie(timestamps, new double[]{5.0, 15.0, 25.0, 35.0, 45.0});
        DoubleTimeSerie close = createSerie(timestamps, new double[]{12.0, 22.0, 32.0, 42.0, 52.0});
        DoubleTimeSerie volume = createSerie(timestamps, new double[]{100.0, 200.0, 300.0, 400.0, 500.0});

        sourceSerie = new CompositeCandleTimeSerie(Stock.SU, TimeFrame.D, open, high, low, close, volume);
    }

    private DoubleTimeSerie createSerie(long[] ts, double[] values) {
        try {
            return new ImmutableDoubleTimeSerie(new StubTimeGrid(ts), values);
        } catch (InvalidTimeSerieException e) {
            throw new RuntimeException("Erreur de création de la série dans le stub de test", e);
        }
    }

    @Test
    @DisplayName("getCandles retourne un set basé sur es dates")
    void getCandles() {

        DataContainer dataContainer = new DataContainer(Stock.SU);
        Arrays.stream(new List[]{List.of(TimeFrame.D)}).forEach(t -> {
                dataContainer.addData(sourceSerie);
        });
        assertEquals(3, dataContainer.getCandles().size());
    }

    private static class StubTimeGrid implements TimeGrid {
        private final long[] timestamps;

        public StubTimeGrid(long[] timestamps) {
            this.timestamps = timestamps;
        }

        @Override
        public int size() {
            return timestamps.length;
        }

        @Override
        public long getTimeStamp(int index) {
            return timestamps[index];
        }
    }
}