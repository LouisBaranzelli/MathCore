package org.quant.definitions.candles;

import org.junit.jupiter.api.BeforeEach;
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

class SliceCompositeCandleTimeSerieTest {

    private CompositeCandleTimeSerie sourceSerie;
    private final long[] timestamps = {1000L, 2000L, 3000L, 4000L, 5000L};

    @BeforeEach
    void setUp() {
        DoubleTimeSerie open = createSerie(timestamps, new double[]{10.0, 20.0, 30.0, 40.0, 50.0});
        DoubleTimeSerie high = createSerie(timestamps, new double[]{15.0, 25.0, 35.0, 45.0, 55.0});
        DoubleTimeSerie low = createSerie(timestamps, new double[]{5.0, 15.0, 25.0, 35.0, 45.0});
        DoubleTimeSerie close = createSerie(timestamps, new double[]{12.0, 22.0, 32.0, 42.0, 52.0});
        DoubleTimeSerie volume = createSerie(timestamps, new double[]{100.0, 200.0, 300.0, 400.0, 500.0});

        sourceSerie = new CompositeCandleTimeSerie(Stock.SU, TimeFrame.D, open, high, low, close, volume);
    }

    // --- Helper Methods & Stubs ---

    private DoubleTimeSerie createSerie(long[] ts, double[] values) {
        try {
            return new ImmutableDoubleTimeSerie(new StubTimeGrid(ts), values);
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

    // --- Tests de Validation (Exceptions dans le constructeur) ---

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si le timestamp 'start' n'existe pas")
    void shouldThrowExceptionWhenStartTimestampNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new SliceCompositeCandleTimeSerie(sourceSerie, 9999L, 4000L)
        );
        assertTrue(ex.getMessage().contains("Timestamp de début (9999) introuvable."));
    }

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si le timestamp 'end' n'existe pas")
    void shouldThrowExceptionWhenEndTimestampNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 9999L)
        );
        assertTrue(ex.getMessage().contains("Timestamp de fin (9999) introuvable."));
    }

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si 'start' est supérieur à 'end'")
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new SliceCompositeCandleTimeSerie(sourceSerie, 4000L, 2000L)
        );
        assertEquals("Le timestamp de début doit être inférieur ou égal au timestamp de fin.", ex.getMessage());
    }

    // --- Tests Fonctionnels ---

    @Test
    @DisplayName("Devrait créer une slice valide et calculer la bonne taille")
    void shouldCreateValidSliceWithCorrectSize() {
        // Slice des timestamps 2000L à 4000L (index 1 à 3 inclus -> taille 3)
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 4000L);

        assertEquals(3, slice.size());
        assertEquals(sourceSerie, slice.getSource());
        assertEquals(2000L, slice.getStart());
        assertEquals(4000L, slice.getEnd());
    }

    @Test
    @DisplayName("Devrait permettre la création d'une slice d'un seul élément (start == end)")
    void shouldAllowSliceOfSingleElement() {
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 3000L, 3000L);

        assertEquals(1, slice.size());
        Candle candle = slice.getCandle(0);
        assertEquals(3000L, candle.timestamp());
        assertEquals(30.0, candle.open());
    }

    @Test
    @DisplayName("Devrait retourner les bonnes bougies décalées par rapport à l'index de la slice")
    void shouldRetrieveCorrectCandlesForRelativeIndex() {
        // Slice de index 1 (2000L) à index 3 (4000L)
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 4000L);

        // Index 0 de la slice correspond à l'index 1 de la source (2000L)
        Candle candle0 = slice.getCandle(0);
        assertEquals(2000L, candle0.timestamp());
        assertEquals(2000L, slice.getFirst().timestamp());
        assertEquals(20.0, candle0.open());

        // Index 2 de la slice correspond à l'index 3 de la source (4000L)
        Candle candle2 = slice.getCandle(2);
        assertEquals(4000L, candle2.timestamp());
        assertEquals(40.0, candle2.open());
        assertEquals(4000L, slice.getLast().timestamp());
    }

    @Test
    @DisplayName("Devrait lever une IndexOutOfBoundsException lors de l'accès à un index invalide")
    void shouldThrowExceptionForOutOfBoundsIndex() {
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 4000L);

        assertThrows(IndexOutOfBoundsException.class, () -> slice.getCandle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getCandle(3)); // Taille = 3, donc index max = 2
    }

    @Test
    @DisplayName("Devrait retourner les sous-séries (OHLV) correctement découpées")
    void shouldReturnCorrectSlicedSubSeries() {
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 4000L);

        DoubleTimeSerie openSlice = slice.getOpenTimeSerie();
        DoubleTimeSerie closeSlice = slice.getCloseTimeSerie();

        assertEquals(3, openSlice.size());
        assertEquals(20.0, openSlice.getValue(0)); // Correspond à 2000L
        assertEquals(40.0, openSlice.getValue(2)); // Correspond à 4000L

        assertEquals(3, closeSlice.size());
        assertEquals(22.0, closeSlice.getValue(0));
        assertEquals(42.0, closeSlice.getValue(2));
    }

    @Test
    @DisplayName("Devrait générer le bon vecteur basé sur la sous-série Close")
    void shouldGenerateVectorFromSlicedCloseSeries() {
        SliceCompositeCandleTimeSerie slice = new SliceCompositeCandleTimeSerie(sourceSerie, 2000L, 4000L);

        Vector vector = slice.toVector();

        assertNotNull(vector);
        assertEquals(3, vector.size());
        assertEquals(22.0, vector.getValue(0));
        assertEquals(32.0, vector.getValue(1));
        assertEquals(42.0, vector.getValue(2));
    }
}