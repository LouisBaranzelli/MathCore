package org.series;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.series.timeserie.TimeFrame;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class TimeToolsTest {

    @Nested
    @DisplayName("Tests de conversion de Duration en long via fromDurationToLong")
    class FromDurationToLongTests {

        @Test
        @DisplayName("Devrait convertir une durée de 30 minutes en secondes")
        void shouldConvert30MinutesToLong() {
            Duration duration = Duration.ofMinutes(30);
            long expectedSeconds = 30 * 60L;
            long result = TimeTools.fromDurationToLong(duration);
            assertEquals(expectedSeconds, result);

            duration = Duration.ofHours(1);
            expectedSeconds = 3600L;
            result = TimeTools.fromDurationToLong(duration);
            assertEquals(expectedSeconds, result);

            duration = Duration.ZERO;
            result = TimeTools.fromDurationToLong(duration);
            assertEquals(0L, result);
        }
    }
    @ParameterizedTest
    @EnumSource(TimeFrame.class)
    @DisplayName("Devrait calculer le step en long (secondes) exact pour chaque TimeFrame")
    void shouldCalculateCorrectStepInLongForEveryTimeFrame(TimeFrame timeFrame) {
        long expectedSeconds = switch (timeFrame) {
            case MI  -> 60L;               // 1 minute
            case MI5 -> 5 * 60L;           // 5 minutes = 300s
            case MI15-> 15 * 60L;          // 15 minutes = 900s
            case MI30-> 30 * 60L;          // 30 minutes = 1800s
            case HR  -> 3600L;             // 1 heure = 3600s
            case D   -> 86400L;            // 1 jour = 86400s
            case WK  -> 7 * 86400L;        // 1 semaine = 604800s
            case MO  -> 31 * 86400L;       // 1 mois standard (Duration basée sur 31 jours)
        };

        long actualSeconds = TimeTools.fromDurationToLong(timeFrame.getDuration());

        // Then
        assertEquals(expectedSeconds, actualSeconds,
                () -> "Échec du calcul pour le timeframe " + timeFrame.getCode());
    }

    @Test
    @DisplayName("Devrait retourner 1 lorsque start == end")
    void shouldReturnOneWhenStartEqualsEnd() {
        long start = 1000L;
        long end = 1000L;

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI);

        assertEquals(1, size, "Pour start == end, il doit y avoir exactement 1 valeur");
    }

    @Test
    @DisplayName("Devrait inverser automatiquement les bornes si start > end")
    void shouldHandleInvertedBoundsCorrectly() {
        // 30 minutes d'écart = 1800 secondes
        long start = 1800L;
        long end = 0L;

        // start > end : l'algorithme doit permuter les bornes et trouver la même taille
        int sizeInverted = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI5); // step = 300s
        int sizeNormal = TimeTools.getNumberValuesStartingFromEndBetween(end, start, TimeFrame.MI5);

        assertEquals(sizeNormal, sizeInverted);
        assertEquals(7, sizeInverted); // 1800 / 300 = 6 pas -> 7 points inclus
    }

    @Test
    @DisplayName("Devrait compter correctement avec un reliquat (écart non multiple du delta)")
    void shouldCountCorrectlyWithRemainder() {
        // Ecart de 700 secondes avec MI5 (delta = 300s)
        // Points comptés en partant de end (700) : 700, 400, 100 (100 - 300 = -200 < 0, donc stop)
        long start = 0L;
        long end = 700L;

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI5);

        assertEquals(3, size);
    }
}

@Nested
@DisplayName("Validation sur tous les TimeFrames")
class AllTimeFramesTests {

    @ParameterizedTest
    @EnumSource(TimeFrame.class)
    @DisplayName("Devrait calculer la bonne taille pour exactement 1 pas complet pour chaque TimeFrame")
    void shouldReturnTwoValuesForExactlyOneStep(TimeFrame timeFrame) {
        long delta = TimeTools.fromDurationToLong(timeFrame.getDuration()); // ou TimeTools.fromDurationToLong(...)
        long start = 0L;
        long end = delta; // exact 1 pas de temps

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame);

        assertEquals(2, size, "Pour un intervalle égal à 1 pas, il doit y avoir 2 points (début et fin)");
    }

    @ParameterizedTest
    @EnumSource(TimeFrame.class)
    @DisplayName("Devrait calculer la bonne taille pour exactement 5 pas complets pour chaque TimeFrame")
    void shouldReturnSixValuesForFiveSteps(TimeFrame timeFrame) {
        long delta = TimeTools.fromDurationToLong(timeFrame.getDuration());
        long start = 0L;
        long end = delta * 5; // exact 5 pas de temps

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame);

        assertEquals(6, size, "Pour 5 pas de temps, il doit y avoir 6 points inclus");
    }

    @Test
    @DisplayName("Devrait convertir '2023-02-24' à minuit UTC en timestamp Unix (secondes)")
    void shouldConvertDateInUtcCorrectly() {
        String day = "2023-02-24";
        ZoneIdEnum zone = ZoneIdEnum.UTC;

        long expectedTimestamp = 1677196800;
        long result = TimeTools.fromDayStringToLong(day, zone);
        assertEquals(expectedTimestamp, result);

        // 9 heures plus tôt qu'UTC -> 1677196800 - (9 * 3600) = 1677174000
        ZoneIdEnum zoneTokyo = ZoneIdEnum.ASIA_TOKYO;
        long expectedTokyoTimestamp = 1677164400;
        long resultTokyo = TimeTools.fromDayStringToLong(day, zoneTokyo);

        assertEquals(24, TimeTools.fromLongToZonedDateTime(resultTokyo, zoneTokyo.getZoneId()).toLocalDate().getDayOfMonth());

        ZonedDateTime backLocalDateTime = TimeTools.fromLongToZonedDateTime(resultTokyo, ZoneIdEnum.ASIA_TOKYO.getZoneId());
        assertEquals(expectedTokyoTimestamp, resultTokyo);

    }

    @ParameterizedTest(name = "Date {0} en zone {1} -> timestamp {2}")
    @CsvSource({
            "2023-01-01, UTC, 1672531200",               // 2023-01-01 00:00:00 UTC
            "2023-02-24, UTC, 1677206400",               // 2023-02-24 00:00:00 UTC
            "2023-02-24, EUROPE_PARIS, 1677196800",      // UTC+1 en hiver (Paris a 1h d'avance sur UTC -> timestamp plus petit)
            "2023-02-24, AMERICA_NEW_YORK, 1677224400"   // UTC-5 en hiver (New York a 5h de retard -> timestamp plus grand)
    })
    @DisplayName("Devrait calculer le bon timestamp pour différentes dates et fuseaux horaires")
    void shouldConvertVariousDatesAndZones(String day, ZoneIdEnum zone, long expectedSeconds) {
        long result = TimeTools.fromDayStringToLong(day, zone);
        assertEquals(expectedSeconds, result,
                () -> String.format("Échec pour la date %s dans la zone %s", day, zone));
    }

    @Test
    @DisplayName("Devrait lever une NullPointerException si un paramètre est null")
    void shouldThrowExceptionWhenParametersAreNull() {
        assertThrows(NullPointerException.class,
                () -> TimeTools.fromDayStringToLong(null, ZoneIdEnum.UTC));

        assertThrows(NullPointerException.class,
                () -> TimeTools.fromDayStringToLong("2023-02-24", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"24-02-2023", "2023/02/24", "2023-13-01", "invalid-date"})
    @DisplayName("Devrait lever une DateTimeParseException si le format de date n'est pas yyyy-MM-dd")
    void shouldThrowExceptionForInvalidDateFormat(String invalidDay) {
        assertThrows(DateTimeParseException.class,
                () -> TimeTools.fromDayStringToLong(invalidDay, ZoneIdEnum.UTC));
    }
}

