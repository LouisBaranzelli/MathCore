package org.series;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.series.timeserie.TimeFrame;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

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

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI, ZoneIdEnum.UTC.getZoneId());

        assertEquals(1, size, "Pour start == end, il doit y avoir exactement 1 valeur");
    }

    @Test
    @DisplayName("Devrait inverser automatiquement les bornes si start > end")
    void shouldHandleInvertedBoundsCorrectly() {
        // 30 minutes d'écart = 1800 secondes
        long start = 1800L;
        long end = 0L;

        // start > end : l'algorithme doit permuter les bornes et trouver la même taille
        int sizeInverted = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI5, ZoneIdEnum.UTC.getZoneId()); // step = 300s
        int sizeNormal = TimeTools.getNumberValuesStartingFromEndBetween(end, start, TimeFrame.MI5, ZoneIdEnum.UTC.getZoneId());

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

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.MI5, ZoneIdEnum.UTC.getZoneId());

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

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame, ZoneIdEnum.UTC.getZoneId());

        assertEquals(2, size, "Pour un intervalle égal à 1 pas, il doit y avoir 2 points (début et fin)");
    }

    @ParameterizedTest
    @EnumSource(TimeFrame.class)
    @DisplayName("Devrait calculer la bonne taille pour exactement 5 pas complets pour chaque TimeFrame")
    void shouldReturnSixValuesForFiveSteps(TimeFrame timeFrame) {
        long delta = TimeTools.fromDurationToLong(timeFrame.getDuration());
        long start = 0L;
        long end = delta * 5; // exact 5 pas de temps

        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame, ZoneIdEnum.UTC.getZoneId());

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
            "2023-01-01, UTC, 1672531200",  // 2023-01-01 00:00:00 UTC
            "2023-02-24, UTC, 1677196800", // 2023-02-24 00:00:00 UTC
            "2023-02-24, EUROPE_PARIS, 1677193200", // 00:00 à Paris = 23:00 UTC la veille (-3600s vs minuit UTC)
            "2023-02-24, AMERICA_NEW_YORK, 1677214800"   // UTC-5 en hiver (New York a 5h de retard -> timestamp plus grand)
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

    @Test
    @DisplayName("Devrait convertir '2023-02-24T15:30:00' en UTC vers le bon timestamp Epoch")
    void shouldConvertStandardIsoFormatInUtc() {
        // Given
        String date = "2023-02-24T15:30:00";
        ZoneIdEnum zone = ZoneIdEnum.UTC;

        // 2023-02-24T00:00:00Z (1677196800) + 15h30 (55800s) = 1677252600
        long expectedTimestamp = 1677252600L;

        long actualTimestamp = TimeTools.fromDateTimeStringToLong(date, zone);

        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @ParameterizedTest(name = "Date {0} en zone {1} -> timestamp {2}")
    @CsvSource({
            // Format complet avec secondes
            "2023-02-24T15:30:00, UTC, 1677252600",
            "2023-02-24T15:30:00, EUROPE_PARIS, 1677249000",      // Paris est UTC+1 en février -> 1h plus tôt qu'UTC (14:30Z)
            "2023-02-24T15:30:00, AMERICA_NEW_YORK, 1677270600",  // NY est UTC-5 en février -> 5h plus tard qu'UTC (20:30Z)

            // Format sans secondes (supporté nativement par ISO_LOCAL_DATE_TIME)
            "2023-02-24T15:30, UTC, 1677252600",
            "2023-02-24T15:30, EUROPE_PARIS, 1677249000"
    })
    @DisplayName("Devrait convertir correctement selon les fuseaux et les variantes ISO")
    void shouldConvertVariousFormatsAndZones(String date, ZoneIdEnum zone, long expectedSeconds) {
        long actual = TimeTools.fromDateTimeStringToLong(date, zone);
        assertEquals(expectedSeconds, actual,
                () -> String.format("Échec du calcul pour la date %s dans la zone %s", date, zone));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2023-02-24 15:30:00", // Espace au lieu du 'T' (lève une exception avec parse par défaut)
            "24/02/2023T15:30:00", // Format de date non ISO
            "2023-02-24",          // Manque la partie heure
            "invalid-datetime"
    })
    @DisplayName("Devrait lever une DateTimeParseException pour des formats non-ISO")
    void shouldThrowExceptionForInvalidFormats(String invalidDate) {
        assertThrows(DateTimeParseException.class,
                () -> TimeTools.fromDateTimeStringToLong(invalidDate, ZoneIdEnum.UTC));
    }

    @Test
    @DisplayName("Devrait lever une NullPointerException si un argument est null")
    void shouldThrowExceptionOnNullInputs() {
        assertThrows(NullPointerException.class,
                () -> TimeTools.fromDateTimeStringToLong(null, ZoneIdEnum.UTC));

        assertThrows(NullPointerException.class,
                () -> TimeTools.fromDateTimeStringToLong("2023-02-24T15:30:00", null));
    }


    @Test
    @DisplayName("Devrait retourner le nombre total de pas quand toutes les dates sont valides")
    void shouldReturnFullCountWhenAllDatesAreValid() {
        // Given : 1 heure d'intervalle (10:00 à 11:00) en pas de 15 minutes -> 5 points (10:00, 10:15, 10:30, 10:45, 11:00)
        long startSeconds = Instant.parse("2026-01-01T10:00:00Z").getEpochSecond();
        long endSeconds = Instant.parse("2026-01-01T11:00:00Z").getEpochSecond();
        TimeFrame timeFrame = TimeFrame.MI15;
        Predicate<Long> alwaysValid = date -> true;

        // When
        int result = TimeTools.getNumberValuesStartingFromEndBetween(startSeconds, endSeconds, timeFrame, alwaysValid, ZoneIdEnum.UTC.getZoneId());

        // Then
        assertEquals(5, result, "Le nombre de pas calculé doit être égal à 5 pour un intervalle de 1h en pas de 15m");
    }

    @Test
    @DisplayName("Devrait exclure correctement les timestamps invalides (ex: filtrage du week-end)")
    void shouldExcludeInvalidTimestamps() {
        // Given : Du Vendredi 2026-01-02 00:00 au Lundi 2026-01-05 00:00 en pas de 1 jour (D1)
        // Dates évaluées : Vendredi (2 janv), Samedi (3 janv), Dimanche (4 janv), Lundi (5 janv) -> 4 points au total
        long startSeconds = Instant.parse("2026-01-02T00:00:00Z").getEpochSecond();
        long endSeconds = Instant.parse("2026-01-05T00:00:00Z").getEpochSecond();
        TimeFrame timeFrame = TimeFrame.D;

        // Predicate : Exclure le Samedi et le Dimanche
        Predicate<Long> excludeWeekends = epochSecond -> {
            DayOfWeek day = Instant.ofEpochSecond(epochSecond)
                    .atZone(ZoneOffset.UTC)
                    .getDayOfWeek();
            return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        };

        int result = TimeTools.getNumberValuesStartingFromEndBetween(startSeconds, endSeconds, timeFrame, excludeWeekends, ZoneIdEnum.UTC.getZoneId());

        // Then : Seuls Vendredi et Lundi sont valides -> 2 points
        assertEquals(2, result, "Les jours de week-end doivent être exclus du comptage");
    }

    @Test
    @DisplayName("Devrait retourner 0 quand aucune date n'est valide")
    void shouldReturnZeroWhenNoDateIsValid() {
        // Given
        long startSeconds = Instant.parse("2026-01-01T00:00:00Z").getEpochSecond();
        long endSeconds = Instant.parse("2026-01-01T01:00:00Z").getEpochSecond();
        TimeFrame timeFrame = TimeFrame.MI15;
        Predicate<Long> neverValid = date -> false;

        int result = TimeTools.getNumberValuesStartingFromEndBetween(startSeconds, endSeconds, timeFrame, neverValid,  ZoneIdEnum.UTC.getZoneId());

        assertEquals(0, result, "Si le prédicat renvoie false systématiquement, le résultat doit être 0");
    }

    @Test
    @DisplayName("Devrait retourner 1 si startSeconds == endSeconds et la date est valide")
    void shouldReturnOneWhenStartEqualsEndAndDateIsValid() {
        // Given
        long timestamp = Instant.parse("2026-01-01T10:00:00Z").getEpochSecond();
        TimeFrame timeFrame = TimeFrame.HR;
        Predicate<Long> alwaysValid = date -> true;

        int result = TimeTools.getNumberValuesStartingFromEndBetween(timestamp, timestamp, timeFrame, alwaysValid,  ZoneIdEnum.UTC.getZoneId());

        assertEquals(1, result, "Un intervalle d'un seul point valide doit retourner 1");
    }

    @Test
    @DisplayName("handle when startSeconds > endSeconds")
    void shouldReturnZeroWhenStartIsAfterEnd() {
        // Given
        long startSeconds = Instant.parse("2026-01-01T12:00:00Z").getEpochSecond();
        long endSeconds = Instant.parse("2026-01-01T10:00:00Z").getEpochSecond();
        TimeFrame timeFrame = TimeFrame.MI5;
        Predicate<Long> alwaysValid = date -> true;

        int result = TimeTools.getNumberValuesStartingFromEndBetween(startSeconds, endSeconds, timeFrame, alwaysValid, ZoneIdEnum.UTC.getZoneId());

        assertEquals(25, result, "Si la date de début est après la date de fin, le résultat doit être 0");
    }

    @ParameterizedTest
    @EnumSource(value = TimeFrame.class, names = {"MI", "MI5", "MI15", "MI30", "HR", "D"})
    @DisplayName("Devrait fonctionner de façon homogène sur les TimeFrames intraday et daily")
    void shouldWorkAcrossStandardTimeFrames(TimeFrame timeFrame) {
        // Given : Intervalle de 24h
        long startSeconds = Instant.parse("2026-01-01T00:00:00Z").getEpochSecond();
        long endSeconds = Instant.parse("2026-01-02T00:00:00Z").getEpochSecond();
        Predicate<Long> alwaysValid = date -> true;

        int result = TimeTools.getNumberValuesStartingFromEndBetween(startSeconds, endSeconds, timeFrame, alwaysValid,  ZoneIdEnum.UTC.getZoneId());

        assertEquals(
                (int) ((endSeconds - startSeconds) / timeFrame.getDuration().getSeconds()) + 1,
                result,
                "Le résultat doit correspondre au découpage théorique sans filtre"
        );
    }
}

