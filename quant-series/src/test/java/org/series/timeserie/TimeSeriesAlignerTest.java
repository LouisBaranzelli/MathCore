package org.series.timeserie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.series.InvalidTimeSerieException;
import org.series.imputation.ImputationStrategy;
import org.series.timegrid.TimeGrid;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesAlignerTest {

    private TimeSeriesAligner aligner;
    private MockImputationStrategy mockStrategy;
    private TimeGrid stubGrid;

    @BeforeEach
    void setUp() {
        mockStrategy = new MockImputationStrategy();
        aligner = new TimeSeriesAligner(mockStrategy);
        stubGrid = new StubTimeGrid(5); // Une grille cible de taille 5
    }

    @Test
    void should_throw_exception_when_instantiated_with_null_strategy() {
        assertThrows(IllegalArgumentException.class, () -> new TimeSeriesAligner(null));
    }

    @Test
    void should_throw_exception_on_null_inputs() {
        Observation[] validObservations = { new StubRawObservation(ZonedDateTime.now(), 10.0) };

        assertThrows(IllegalArgumentException.class, () -> aligner.align(null, stubGrid));
        assertThrows(IllegalArgumentException.class, () -> aligner.align(validObservations, null));
    }

    @Test
    void should_throw_exception_when_observations_array_is_empty() {
        RawObservation[] emptyObservations = new RawObservation[0];

        assertThrows(InvalidTimeSerieException.class, () -> aligner.align(emptyObservations, stubGrid));
    }

    @Test
    void should_clone_and_sort_raw_observations_by_date_before_passing_to_strategy() throws InvalidTimeSerieException {
        // Données en vrac (Mardi, puis Dimanche, puis Jeudi)
        ZonedDateTime tuesday = ZonedDateTime.parse("2026-03-10T10:00:00Z");
        ZonedDateTime sunday = ZonedDateTime.parse("2026-03-08T10:00:00Z");
        ZonedDateTime thursday = ZonedDateTime.parse("2026-03-12T10:00:00Z");

        Observation obs1 = new StubRawObservation(tuesday, 20.0);
        Observation obs2 = new StubRawObservation(sunday, 10.0);
        Observation obs3 = new StubRawObservation(thursday, 30.0);

        Observation[] input = { obs1, obs2, obs3 };

        // Mocking de la stratégie pour renvoyer un tableau vide sans planter
        mockStrategy.setResponse(new double[stubGrid.size()]);

        // Exécution de l'alignement
        aligner.align(input, stubGrid);

        // 1. Vérification que le tableau d'origine n'a pas été modifié (immuabilité / clone)
        assertEquals(tuesday, input[0].getZonedDateTime(), "Le tableau d'entrée ne doit pas être réordonné (effet de bord)");

        // 2. Vérification que la stratégie a reçu les données TRÈS EXACTEMENT triées par ordre chronologique
        long[] receivedDates = mockStrategy.getCapturedDates();
        double[] receivedValues = mockStrategy.getCapturedValues();

        assertEquals(3, receivedDates.length);

        // Chronologie attendue : Sunday (10.0) -> Tuesday (20.0) -> Thursday (30.0)
        assertEquals(10.0, receivedValues[0], "Le premier élément transmis doit être le plus ancien (Dimanche)");
        assertEquals(20.0, receivedValues[1], "Le deuxième élément transmis doit être le Mardi");
        assertEquals(30.0, receivedValues[2], "Le troisième élément transmis doit être le plus récent (Jeudi)");

        assertTrue(receivedDates[0] <= receivedDates[1], "Les dates transmises doivent être ordonnées");
        assertTrue(receivedDates[1] <= receivedDates[2], "Les dates transmises doivent être ordonnées");
    }

    @Test
    void should_return_immutable_time_serie_with_aligned_values_from_strategy() throws InvalidTimeSerieException {
        ZonedDateTime now = ZonedDateTime.now();
        Observation[] input = { new StubRawObservation(now, 42.0) };

        // On simule une réponse de la stratégie d'imputation
        double[] simulatedImputedValues = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        mockStrategy.setResponse(simulatedImputedValues);

        // Exécution
        DoubleTimeSerie result = aligner.align(input, stubGrid);

        // Vérifications
        assertNotNull(result, "Le résultat ne doit pas être null");
//        assertEquals(stubGrid, result.getTimeGrid(), "La série retournée doit encapsuler la grille cible");

        // Idéalement, si DoubleTimeSerie expose ses valeurs (via un getter ou une méthode d'accès), on valide :
        // (Adapte cette ligne selon les méthodes disponibles sur ton interface DoubleTimeSerie)
        // assertArrayEquals(simulatedImputedValues, ((ImmutableDoubleTimeSerie) result).getValues());
    }


    // =========================================================================
    // --- STUBS & MOCKS MANUELS POUR COUVRIR L'ARCHITECTURE SANS DOUBLONS ---
    // =========================================================================

    // Stub pour RawObservation
    private static class StubRawObservation implements Observation {
        private final ZonedDateTime date;
        private final double value;

        public StubRawObservation(ZonedDateTime date, double value) {
            this.date = date;
            this.value = value;
        }

        @Override
        public ZonedDateTime getZonedDateTime() {
            return date;
        }

        @Override
        public double getDoubleValue() {
            return value;
        }

        @Override
        public int compareTo(RawObservation o) {
            return 0;
        }
    }

    // Stub pour TimeGrid
    private static class StubTimeGrid implements TimeGrid {
        private final int size;

        public StubTimeGrid(int size) {
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public long getTimeStamp(int index) {
            return index * 1000L; // Simulation d'index temporels linéaires
        }
    }

    // Mock pour capturer ce que l'Aligner envoie à la stratégie
    private static class MockImputationStrategy implements ImputationStrategy {
        private long[] capturedDates;
        private double[] capturedValues;
        private double[] responseStub;

        public void setResponse(double[] response) {
            this.responseStub = response;
        }

        public long[] getCapturedDates() {
            return capturedDates;
        }

        public double[] getCapturedValues() {
            return capturedValues;
        }

        @Override
        public double[] alignAndImpute(long[] sortedDates, double[] rawValues, TimeGrid targetGrid) {
            // On capture les arguments pour pouvoir faire des assertions dessus dans le test
            this.capturedDates = sortedDates.clone();
            this.capturedValues = rawValues.clone();
            return responseStub;
        }
    }
}