package org.series;

import org.junit.jupiter.api.Test;
import org.math.vector.Vector;
import org.series.timegrid.TimeGrid;
import org.series.timeserie.ImmutableDoubleTimeSerie;
import org.series.timeserie.DoubleTimeSerie;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableDoubleTimeSerieTest {

    @Test
    void shouldThrowExceptionWhenTimeGridIsNull() {
        // Validation que le constructeur refuse un TimeGrid null
        assertThrows(NullPointerException.class, () ->
                new ImmutableDoubleTimeSerie(null, new double[]{10.0}));
    }

    @Test
    void shouldThrowExceptionWhenValuesArrayIsNull() {
        TimeGrid stubGrid = new StubTimeGrid(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(stubGrid, null));
        assertTrue(ex.getMessage().contains("Arrays cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenLengthsMismatch() {
        TimeGrid stubGrid = new StubTimeGrid(2); // Taille de la grille = 2
        double[] values = {10.5}; // Taille des valeurs = 1

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(stubGrid, values));
        assertTrue(ex.getMessage().contains("Mismatched lengths"));
    }

    @Test
    void shouldThrowExceptionWhenValueIsNaN() {
        TimeGrid stubGrid = new StubTimeGrid(2);
        double[] withNaN = {10.5, Double.NaN};

        InvalidTimeSerieException ex = assertThrows(InvalidTimeSerieException.class, () ->
                new ImmutableDoubleTimeSerie(stubGrid, withNaN));
        assertTrue(ex.getMessage().contains("Invalid numeric value (NaN/Infinite)"));
    }

    @Test
    void shouldThrowExceptionWhenValueIsInfinite() {
        TimeGrid stubGrid = new StubTimeGrid(2);
        double[] withInfinity = {Double.POSITIVE_INFINITY, 12.0};

        InvalidTimeSerieException ex = assertThrows(InvalidTimeSerieException.class, () ->
                new ImmutableDoubleTimeSerie(stubGrid, withInfinity));
        assertTrue(ex.getMessage().contains("Invalid numeric value (NaN/Infinite)"));
    }

    @Test
    void shouldStoreAndRetrieveDataCorrectly() throws InvalidTimeSerieException {
        long[] timestamps = {1000L, 2000L, 3000L};
        TimeGrid stubGrid = new StubTimeGrid(timestamps);
        double[] values = {10.5, 11.2, 12.8};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(stubGrid, values);

        assertEquals(3, serie.size());
        assertEquals(10.5, serie.getValue(0));
        assertEquals(3000L, serie.getTimestamp(2));
    }

    @Test
    void shouldBeProtectedAgainstExternalSourceMutation() throws InvalidTimeSerieException {
        long[] timestamps = {1000L, 2000L};
        TimeGrid stubGrid = new StubTimeGrid(timestamps);
        double[] values = {10.0, 20.0};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(stubGrid, values);

        // Tentative de modification du tableau d'origine après construction
        values[0] = -99.9;

        // Grâce au clone() dans le constructeur, la valeur encapsulée reste intacte
        assertEquals(10.0, serie.getValue(0), "La série doit être protégée contre les mutations externes");
    }

    @Test
    void shouldConvertToVectorSuccessfully() throws InvalidTimeSerieException {
        // Given
        long[] timestamps = {1000L, 2000L, 3000L};
        TimeGrid stubGrid = new StubTimeGrid(timestamps);
        double[] values = {10.5, -2.3, 42.0};
        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(stubGrid, values);

        // When
        Vector vector = serie.toVector();

        // Then
        assertNotNull(vector, "Le vecteur généré ne doit pas être null");
        assertEquals(serie.size(), vector.size(), "Le vecteur doit avoir la même taille que la série");

        for (int i = 0; i < values.length; i++) {
            assertEquals(serie.getValue(i), vector.getValue(i),
                    "La valeur du vecteur à l'index " + i + " doit être identique à celle de la série");
        }
    }

    // --- Stub de TimeGrid requis pour alimenter la série temporelle ---
    private static class StubTimeGrid implements TimeGrid {
        private final long[] timestamps;

        // Constructeur par défaut à taille fixe
        public StubTimeGrid(int size) {
            this.timestamps = new long[size];
            for (int i = 0; i < size; i++) {
                this.timestamps[i] = i * 1000L;
            }
        }

        // Constructeur personnalisé avec timestamps définis
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