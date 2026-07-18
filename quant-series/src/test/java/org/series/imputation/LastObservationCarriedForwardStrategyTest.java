package org.series.imputation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LastObservationCarriedForwardStrategyTest {

    private LastObservationCarriedForwardStrategy strategy;
    private final long maxTolerance = 100L; // 100 unités de temps de tolérance max
    private final double defaultMaxPercentMissing = 0.50; // 50% de données manquantes tolérées par défaut

    @BeforeEach
    void setUp() {
        // Initialisation par défaut avec 50% de tolérance aux trous
        strategy = new LastObservationCarriedForwardStrategy(maxTolerance, defaultMaxPercentMissing);
    }

    @Test
    void should_throw_exception_when_instantiated_with_invalid_tolerance() {
        assertThrows(IllegalArgumentException.class, () -> new LastObservationCarriedForwardStrategy(0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new LastObservationCarriedForwardStrategy(-10, 0.5));
    }

    @Test
    void should_throw_exception_when_instantiated_with_invalid_percent_missing() {
        assertThrows(IllegalArgumentException.class, () -> new LastObservationCarriedForwardStrategy(100L, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new LastObservationCarriedForwardStrategy(100L, 1.05));
    }

    @Test
    void should_throw_exception_on_null_inputs() {
        TimeGrid stubGrid = new StubTimeGrid(new long[]{100L});

        assertThrows(IllegalArgumentException.class, () -> strategy.alignAndImpute(null, new double[]{1.0}, stubGrid));
        assertThrows(IllegalArgumentException.class, () -> strategy.alignAndImpute(new long[]{100L}, null, stubGrid));
        assertThrows(IllegalArgumentException.class, () -> strategy.alignAndImpute(new long[]{100L}, new double[]{1.0}, null));
    }

    @Test
    void should_throw_exception_when_arrays_have_different_lengths() {
        TimeGrid stubGrid = new StubTimeGrid(new long[]{100L});
        long[] dates = {100L, 200L};
        double[] values = {1.5};

        assertThrows(IllegalArgumentException.class, () -> strategy.alignAndImpute(dates, values, stubGrid));
    }

    @Test
    void should_throw_exception_when_raw_dates_are_not_sorted() {
        TimeGrid stubGrid = new StubTimeGrid(new long[]{100L, 200L});
        long[] unsortedDates = {200L, 100L};
        double[] values = {2.0, 1.0};

        assertThrows(IllegalArgumentException.class, () -> strategy.alignAndImpute(unsortedDates, values, stubGrid));
    }

    @Test
    void should_throw_exception_when_target_grid_is_not_strictly_increasing() {
        TimeGrid badGrid = new StubTimeGrid(new long[]{100L, 100L});
        long[] dates = {100L};
        double[] values = {1.0};

        assertThrows(IllegalStateException.class, () -> strategy.alignAndImpute(dates, values, badGrid));
    }

    @Test
    void should_return_nan_when_target_grid_starts_before_first_raw_date_and_below_threshold() throws InvalidTimeSerieException {
        // Grille de 4 points. Le premier point (t=50) est un trou (1/4 = 25% de trous).
        // Notre stratégie par défaut accepte 50%, donc pas de crash.
        TimeGrid targetGrid = new StubTimeGrid(new long[]{50L, 100L, 150L, 200L});
        long[] rawDates = {100L};
        double[] rawValues = {42.0};

        double[] result = strategy.alignAndImpute(rawDates, rawValues, targetGrid);

        // Assertions sur le contenu du tableau retourné
        assertTrue(Double.isNaN(result[0]), "La valeur à l'index 0 devrait être NaN");
        assertEquals(42.0, result[1]);
        assertEquals(42.0, result[2]);
        assertEquals(42.0, result[3]);
    }

    @Test
    void should_trigger_circuit_breaker_when_missing_value_ratio_exceeded_in_progress() {
        // Grille cible de 5 points.
        TimeGrid targetGrid = new StubTimeGrid(new long[]{50L, 60L, 70L, 100L, 200L});
        long[] rawDates = {100L};
        double[] rawValues = {42.0};

        // On configure un disjoncteur très strict : maximum 20% de trous (soit max 1 trou sur les 5 de la grille)
        LastObservationCarriedForwardStrategy strictStrategy = new LastObservationCarriedForwardStrategy(maxTolerance, 0.20);

        // Au fil de l'eau :
        // i = 0 (t=50) -> Trou 1. Ratio = 1/5 (20%). Égal au seuil -> OK.
        // i = 1 (t=60) -> Trou 2. Ratio = 2/5 (40%). 40% > 20% -> Le disjoncteur doit sauter immédiatement à i = 1 !
        assertThrows(InvalidTimeSerieException.class, () -> strictStrategy.alignAndImpute(rawDates, rawValues, targetGrid));
    }

    @Test
    void should_forward_fill_values_within_tolerance_and_put_nan_when_expired_without_crashing() throws InvalidTimeSerieException {
        // Grille de 5 points, 1 seul trou provoqué par l'expiration de la tolérance à t=201 (index 3)
        // Ratio de trous final : 1/5 = 20%. Notre stratégie tolère 50%, donc pas de crash.
        TimeGrid targetGrid = new StubTimeGrid(new long[]{100L, 150L, 200L, 201L, 300L});
        long[] rawDates = {100L, 300L};
        double[] rawValues = {10.0, 20.0};

        double[] result = strategy.alignAndImpute(rawDates, rawValues, targetGrid);

        assertEquals(10.0, result[0]);
        assertEquals(10.0, result[1]);
        assertEquals(10.0, result[2]);
        assertTrue(Double.isNaN(result[3]), "La valeur à l'index 3 devrait être NaN (périmée)");
        assertEquals(20.0, result[4]);
    }

    @Test
    void should_handle_multiple_raw_ticks_falling_in_same_grid_interval() throws InvalidTimeSerieException {
        TimeGrid targetGrid = new StubTimeGrid(new long[]{100L, 200L});
        long[] rawDates = {100L, 120L, 150L};
        double[] rawValues = {1.0, 2.0, 3.0};

        double[] result = strategy.alignAndImpute(rawDates, rawValues, targetGrid);

        assertEquals(1.0, result[0]);
        assertEquals(3.0, result[1]);
    }

    // --- Stub minimal pour TimeGrid ---
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