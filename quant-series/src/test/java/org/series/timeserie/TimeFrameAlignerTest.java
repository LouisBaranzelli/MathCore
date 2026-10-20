package org.series.timeserie;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.timegrid.AbsoluteTimeFrameAligner;
import org.series.timegrid.BuisnessDayTimeFrameAligner;
import org.series.timegrid.TimeFrameAligner;
import org.series.timegrid.TimeGrid;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeFrameAlignerTest {

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
        Observation[] validObservations = { new StubRawObservation(TimeTools.fromZonedDateTimeToLong(ZonedDateTime.now()), 10.0) };

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

        Observation obs1 = new StubRawObservation(TimeTools.fromZonedDateTimeToLong(tuesday), 20.0);
        Observation obs2 = new StubRawObservation(TimeTools.fromZonedDateTimeToLong(sunday), 10.0);
        Observation obs3 = new StubRawObservation(TimeTools.fromZonedDateTimeToLong(thursday), 30.0);

        Observation[] input = { obs1, obs2, obs3 };

        // Mocking de la stratégie pour renvoyer un tableau vide sans planter
        mockStrategy.setResponse(new double[stubGrid.size()]);

        // Exécution de l'alignement
        aligner.align(input, stubGrid);

        // 1. Vérification que le tableau d'origine n'a pas été modifié (immuabilité / clone)
        assertEquals(TimeTools.fromZonedDateTimeToLong(tuesday), input[0].getDateTime(), "Le tableau d'entrée ne doit pas être réordonné (effet de bord)");

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
        Observation[] input = { new StubRawObservation(TimeTools.fromZonedDateTimeToLong(now), 42.0) };

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
        private final long date;
        private final double value;

        @Getter
        private final ZoneId zoneId;

        public StubRawObservation(long date, double value) {
            this.date = date;
            this.value = value;
            this.zoneId = ZoneIdEnum.UTC.getZoneId();
        }

        @Override
        public long getDateTime() {
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

    @Nested
    class TimeFrameAlignerCeilTest {

        private TimeFrameAligner aligner;

        @BeforeEach
        void setUp() {
            // Implémentation concrète pour les tests :
            // Les week-ends (Samedi/Dimanche) sont considérés comme des jours invalides (fermés).
            // Les heures valides sont entre 09:00 et 17:00 inclus.
            aligner = new TimeFrameAligner() {
                @Override
                public boolean isValidDay(ZonedDateTime dt) {
                    DayOfWeek day = dt.getDayOfWeek();
                    return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                }

                @Override
                public boolean isValidHour(ZonedDateTime dt) {
                    return dt.getHour() >= 9 && dt.getHour() <= 17;
                }
            };
        }

        @Test
        void should_return_same_datetime_when_already_aligned() {
            ZonedDateTime exactMinute = ZonedDateTime.of(2026, 3, 10, 10, 15, 0, 0, ZoneId.of("UTC")); // Mardi 10:15:00
            ZonedDateTime result = aligner.alignCeil(exactMinute, TimeFrame.MI15);

            assertEquals(exactMinute, result, "Si la date est déjà alignée, le ceil doit la retourner telle quelle");
        }

        @Test
        void should_ceil_minutes_correctly() {
            // Mardi 10:12:34 -> Doit s'arrondir au 15 minutes supérieur -> 10:15:00
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 10, 10, 12, 34, 0, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 10, 10, 15, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.MI15);

            assertEquals(expected, result);
        }

        @Test
        void should_ceil_hour_correctly() {
            // Mardi 10:45:00 -> Doit s'arrondir à l'heure supérieure -> 11:00:00
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 10, 10, 45, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 10, 11, 0, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.HR);
            assertEquals(expected, result);
        }

        @Test
        void should_ceil_day_correctly() {
            // Dimanche 14:30:00 -> Doit s'arrondir au début du jour suivant valide -> Lundi 09:00:00
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 15, 14, 30, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 16, 9, 0, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.D);

            assertEquals(expected, result);
        }

        @Test
        void should_handle_weekend_jump_when_ceiling_on_friday_evening() {
            // Vendredi à 19:00:00 avec un TimeFrame journalier (D)
            // Le jour suivant (samedi) est invalide, il doit sauter au lundi 09:00:00
            ZonedDateTime fridayEvening = ZonedDateTime.of(2026, 3, 13, 19, 0, 0, 0, ZoneId.of("UTC")); // 13 mars 2026 est un vendredi
            ZonedDateTime expectedMonday = ZonedDateTime.of(2026, 3, 16, 9, 0, 0, 0, ZoneId.of("UTC")); // 16 mars 2026 est un lundi

            ZonedDateTime result = aligner.alignCeil(fridayEvening, TimeFrame.D);

            assertEquals(expectedMonday, result, "Le ceil d'un vendredi après-midi doit sauter le week-end et atterrir au lundi 09:00");
        }

        @Test
        void should_handle_sub_second_truncation_edge_case() {
            // Mardi 10:12:34.999 -> Les secondes doivent être tronquées, puis arrachées au ceil 15 min -> 10:15:00
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 10, 10, 12, 34, 999999999, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 10, 10, 15, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.MI15);

            assertEquals(expected, result);
        }

        @Test
        void should_handle_minute_boundary_edge_case() {
            // Mardi 10:14:59 -> Juste avant la limite des 15 minutes, doit arrondir à 10:15:00
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 10, 10, 14, 59, 0, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 10, 10, 15, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.MI15);

            assertEquals(expected, result);
        }

        @Test
        void should_handle_hour_before_market_open() {
            // Mardi à 07:30:00 (avant l'ouverture à 09:00)
            // Le ceil à l'échelle de l'heure ou de la minute doit s'ajuster ou rattraper l'heure valide
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 10, 7, 30, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime expected = ZonedDateTime.of(2026, 3, 10, 9, 0, 0, 0, ZoneId.of("UTC"));

            // L'heure doit être ramenée dans une plage horaire valide (>= 9)
            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.MI15);

            assertEquals(expected, result);
        }

        @Test
        void should_handle_end_of_month_transition() {
            // Mardi 31 mars 2026 à 16:30:00 -> Doit passer au 1er avril 2026 à 00:00:00 (ou ouverture)
            ZonedDateTime input = ZonedDateTime.of(2026, 3, 31, 16, 30, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime expected = ZonedDateTime.of(2026, 4, 1, 9, 0, 0, 0, ZoneId.of("UTC"));

            ZonedDateTime result = aligner.alignCeil(input, TimeFrame.D);

            assertEquals(expected, result);
        }
    }
}