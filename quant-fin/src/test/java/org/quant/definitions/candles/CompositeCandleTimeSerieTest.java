package org.quant.definitions.candles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.Vector;
import org.quant.definitions.assets.Stock;
import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.ImmutableDoubleTimeSerie;
import org.series.timeserie.TimeFrame;


import static org.junit.jupiter.api.Assertions.*;

class CompositeCandleTimeSerieTest {

    // --- Helper Methods & Stubs ---

    private DoubleTimeSerie createSerie(long[] timestamps, double[] values) {
        try {
            return new ImmutableDoubleTimeSerie(new StubTimeGrid(timestamps), values);
        } catch (InvalidTimeSerieException e) {
            throw new RuntimeException("Erreur de création de la série dans le stub de test", e);
        }
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



    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si une des séries est null")
    void shouldThrowExceptionWhenAnySeriesIsNull() {
        long[] timestamps = {1000L};
        double[] values = {10.0};
        DoubleTimeSerie validSeries = createSerie(timestamps, values);

        // Test avec 'open' à null
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new CompositeCandleTimeSerie(Stock.SU, TimeFrame.D, null, validSeries, validSeries, validSeries, validSeries)
        );
        assertEquals("Input time series cannot be null.", ex.getMessage());
    }

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException en cas d'écart de taille entre les séries")
    void shouldThrowExceptionWhenSeriesSizesMismatch() {
        long[] timestamps3 = {1000L, 2000L, 3000L};
        long[] timestamps2 = {1000L, 2000L};

        DoubleTimeSerie seriesOf3 = createSerie(timestamps3, new double[]{10.0, 11.0, 12.0});
        DoubleTimeSerie seriesOf2 = createSerie(timestamps2, new double[]{10.0, 11.0});

        // La série Open a une taille de 2 au lieu de 3
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new CompositeCandleTimeSerie(Stock.SU, TimeFrame.D, seriesOf2, seriesOf3, seriesOf3, seriesOf3, seriesOf3)
        );
        assertEquals("All input DoubleTimeSeries must have strictly equal sizes.", ex.getMessage());
    }

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si un timestamp ne concorde pas")
    void shouldThrowExceptionWhenTimestampsAreMisaligned() {
        long[] baseTimestamps = {1000L, 2000L, 3000L};
        long[] corruptedTimestamps = {1000L, 2003L, 3000L}; // Incohérence à l'index 1

        DoubleTimeSerie open = createSerie(baseTimestamps, new double[]{10.0, 11.0, 12.0});
        DoubleTimeSerie high = createSerie(corruptedTimestamps, new double[]{12.0, 13.0, 14.0});
        DoubleTimeSerie low = createSerie(baseTimestamps, new double[]{9.0, 10.0, 11.0});
        DoubleTimeSerie close = createSerie(baseTimestamps, new double[]{11.0, 12.0, 13.0});
        DoubleTimeSerie volume = createSerie(baseTimestamps, new double[]{100.0, 200.0, 150.0});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new CompositeCandleTimeSerie(Stock.SU, TimeFrame.D, open, high, low, close, volume)
        );

        assertTrue(ex.getMessage().contains("Timestamp misalignment detected at index 1"));
    }

    // --- Tests de comportement fonctionnel ---

    @Test
    @DisplayName("Devrait initialiser les attributs et construire correctement les Candles à un index donné")
    void shouldConstructAndRetrieveCandleCorrectly() {
        // Given
        long[] timestamps = {1609459200000L, 1609545600000L}; // Exemples d'epoch millis
        DoubleTimeSerie open = createSerie(timestamps, new double[]{100.0, 105.0});
        DoubleTimeSerie high = createSerie(timestamps, new double[]{110.0, 115.0});
        DoubleTimeSerie low = createSerie(timestamps, new double[]{95.0, 100.0});
        DoubleTimeSerie close = createSerie(timestamps, new double[]{105.0, 112.0});
        DoubleTimeSerie volume = createSerie(timestamps, new double[]{1000.0, 1500.0});

        // When
        CompositeCandleTimeSerie composite = new CompositeCandleTimeSerie(
                Stock.SU, TimeFrame.D, open, high, low, close, volume
        );

        // Then
        assertEquals(Stock.SU, composite.getInstrument());
        assertEquals(TimeFrame.D, composite.getTimeframe());

        Candle candle = composite.getCandle(1);
        assertNotNull(candle);
        assertEquals(Stock.SU, candle.instrument());
        assertEquals(1609545600000L, candle.timestamp());
        assertEquals(105.0, candle.open());
        assertEquals(115.0, candle.high());
        assertEquals(100.0, candle.low());
        assertEquals(112.0, candle.close());
        assertEquals(1500.0, candle.volume());
    }

    @Test
    @DisplayName("Devrait déléguer la création du vecteur à la série Close")
    void shouldConvertToVectorDelegatingToCloseSerie() {
        // Given
        long[] timestamps = {1000L, 2000L};
        DoubleTimeSerie open = createSerie(timestamps, new double[]{10.0, 20.0});
        DoubleTimeSerie high = createSerie(timestamps, new double[]{15.0, 25.0});
        DoubleTimeSerie low = createSerie(timestamps, new double[]{5.0, 15.0});
        DoubleTimeSerie close = createSerie(timestamps, new double[]{12.0, 22.0});
        DoubleTimeSerie volume = createSerie(timestamps, new double[]{100.0, 200.0});

        CompositeCandleTimeSerie composite = new CompositeCandleTimeSerie(
                Stock.SU, TimeFrame.HR, open, high, low, close, volume
        );

        // When
        Vector vector = composite.toVector();

        // Then
        assertNotNull(vector);
        assertEquals(close.size(), vector.size());
        assertEquals(12.0, vector.getValue(0));
        assertEquals(22.0, vector.getValue(1));
    }
}