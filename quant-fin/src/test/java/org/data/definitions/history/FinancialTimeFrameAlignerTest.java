package org.data.definitions.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.series.timeserie.TimeFrame;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tests des cas limites pour FinancialTimeFrameAligner avec TimeUtils réel")
class FinancialTimeFrameAlignerTest {

    private FinancialTimeFrameAligner aligner;
    private static final ZoneId UTC = ZoneId.of("UTC");


    // =========================================================================
    // 1. TIMEFRAMES INTRADAY (MI, MI5, MI15, MI30, HR)
    // =========================================================================
    @Nested
    @DisplayName("Alignement Intraday & Frontières de minutes")
    class IntradayTests {

        @ParameterizedTest(name = "MI5 sur minute {0} -> minute attendue {1}")
        @CsvSource({
                "00, 00", "01, 00", "04, 00",
                "05, 05", "9, 05", "10, 10",
                "29, 25", "30, 30", "59, 55"
        })
        @DisplayName("Frontières exactes pour MI5 sur un jour ouvré")
        void testMI5BoundaryMinutes(int minuteInput, int expectedMinute) {
            ZonedDateTime input = ZonedDateTime.of(2024, 6, 11, 14, minuteInput, 59, 999_999_999, UTC); // Mardi ouvré
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.MI5);

            assertEquals(expectedMinute, result.getMinute());
            assertEquals(0, result.getSecond(), "Les secondes doivent être réinitialisées à 0");
            assertEquals(0, result.getNano(), "Les nanosecondes doivent être réinitialisées à 0");
        }

        @ParameterizedTest(name = "MI15 sur minute {0} -> minute attendue {1}")
        @CsvSource({"00, 00", "14, 00", "15, 15", "44, 30", "45, 45", "59, 45"})
        void testMI15Boundaries(int minuteInput, int expectedMinute) {
            ZonedDateTime input = ZonedDateTime.of(2024, 6, 11, 10, minuteInput, 30, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.MI15);
            assertEquals(expectedMinute, result.getMinute());
        }

        @Test
        @DisplayName("Retourne sur la fin du bon jour ouvré")
        void testRetourneSurLeBonJourOuvre() {
            ZonedDateTime input = ZonedDateTime.of(2026, 8, 8, 16, 59, 59, 999_999_999, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.MI5);

            assertEquals(8, result.getDayOfMonth());
            assertEquals(0, result.getHour());
            assertEquals(0, result.getMinute());
            assertEquals(0, result.getSecond());
        }

        @Test
        @DisplayName("HR (Hourly) réinitialise minutes et secondes")
        void testHourly() {
            ZonedDateTime input = ZonedDateTime.of(2024, 6, 11, 16, 59, 59, 999_999_999, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.HR);

            assertEquals(16, result.getHour());
            assertEquals(0, result.getMinute());
            assertEquals(0, result.getSecond());
        }

        @Test
        @DisplayName("Intraday un Samedi 15 Juin 2024 -> Recule au Samedi 00:00 (Fin du Vendredi)")
        void testHourlyWeekend() {
            ZonedDateTime input = ZonedDateTime.of(2024, 6, 15, 16, 59, 59, 999_999_999, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.HR);

            assertEquals(14, result.getDayOfMonth());
            assertEquals(23, result.getHour());
            assertEquals(0, result.getMinute());
            assertEquals(0, result.getSecond());

            input = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
            result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.HR);
            // derniere heure de l'année est le 1er à minuit (avec un 31 est ouvré)
            assertEquals(31, result.getDayOfMonth());
            assertEquals(23, result.getHour());
            assertEquals(0, result.getMinute());
            assertEquals(0, result.getSecond());

        }

        @Test
        @DisplayName("Intraday un Samedi -> Recule au Vendredi - la periode du time frame (Fin du Vendredi)")
        void testIntradayOnSaturday() {
            // Samedi 15 Juin 2024 à 14h25
            ZonedDateTime saturday = ZonedDateTime.of(2024, 6, 15, 14, 25, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(saturday, TimeFrame.MI5);

            assertEquals(2024, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(14, result.getDayOfMonth()); // Samedi 15 à 00:00 = Fin du Vendredi 14
            assertEquals(23, result.getHour());
            assertEquals(55, result.getMinute());
        }
    }

    // =========================================================================
    // 2. TIMEFRAME JOURNALIER (D) & JOURS FÉRIÉS FIXES
    // =========================================================================
    @Nested
    @DisplayName("Alignement Journalier (D)")
    class DailyTests {

        @Test
        @DisplayName("Jour ouvré ordinaire (Mardi 14h30) -> Mardi 00:00")
        void testBusinessDay() {
            ZonedDateTime tuesday = ZonedDateTime.of(2024, 6, 11, 14, 30, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(tuesday, TimeFrame.D);

            assertEquals(11, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Samedi 14h00 -> Vendredi 00:00 (Fin du Vendredi)")
        void testSaturday() {
            ZonedDateTime saturday = ZonedDateTime.of(2024, 6, 15, 14, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(saturday, TimeFrame.D);

            assertEquals(14, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Dimanche 20h00 -> Vendredi 00:00 (journée du Vendredi)")
        void testSunday() {
            ZonedDateTime sunday = ZonedDateTime.of(2024, 6, 16, 20, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(sunday, TimeFrame.D);

            assertEquals(14, result.getDayOfMonth()); // Vendredi 14 à 00:00 = Fin du Vendredi 14
        }

        @Test
        @DisplayName("Jour férié fixe (1er Mai 2024 - Mercredi) -> Mercredi 00:00 (Fin du Mardi 30 Avril)")
        void testMayFirstHoliday() {
            ZonedDateTime mayFirst = ZonedDateTime.of(2024, 5, 1, 14, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(mayFirst, TimeFrame.D);

            // Le 1er mai étant férié, recule au minuit du jour précédent Mardi 30 Avril -> 30 Avril 00:00:00
            assertEquals(4, result.getMonthValue());
            assertEquals(30, result.getDayOfMonth());
            assertEquals(0, result.getHour());

            ZonedDateTime maySecond = ZonedDateTime.of(2024, 5, 2, 14, 0, 0, 0, UTC);
            result = FinancialTimeFrameAligner.alignFloor(maySecond, TimeFrame.D);
            assertEquals(5, result.getMonthValue());
            assertEquals(2, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Jour férié suivi du Jour de l'an (31 Déc 2023 Dimanche + 1er Janv 2024 Lundi) -> Samedi 30 Déc 00:00")
        void testNewYearHolidayTransition() {
            // Lundi 1er Janvier 2024 (Férié fixe)
            ZonedDateTime newYearDay = ZonedDateTime.of(2024, 1, 1, 15, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(newYearDay, TimeFrame.D);

            // Dimanche 31 Déc et Lundi 1er Janv sont non-ouvrés -> Recule jusqu'au Vendredi 29 Déc 00:00 (Fin du Vendredi 29 Déc)
            assertEquals(2023, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(29, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }
    }

    // =========================================================================
    // 3. TIMEFRAME HEBDOMADAIRE (WK)
    // =========================================================================
    @Nested
    @DisplayName("Alignement Hebdomadaire (WK)")
    class WeeklyTests {

        @ParameterizedTest(name = "Jour {0} de la semaine en cours -> Clôture de la semaine précédente")
        @ValueSource(ints = {10, 11, 12, 13}) // Lundi 10 au Jeudi 13 Juin 2024
        void testUnclosedWeekDays(int dayOfMonth) {
            ZonedDateTime input = ZonedDateTime.of(2024, 6, dayOfMonth, 12, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(input, TimeFrame.WK);

            // Semaine non finie -> renvoie la clôture de la semaine précédente (Vendredi 7 Juin 00:00)
            assertEquals(6, result.getMonthValue());
            assertEquals(7, result.getDayOfMonth());
            assertEquals(0, result.getHour());

        }

        @Test
        @DisplayName("Samedi (15 Juin) -> Semaine clôturée -> Renvoie Vendredi 14 Juin 00:00")
        void testClosedWeekSaturday() {
            ZonedDateTime saturday = ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(saturday, TimeFrame.WK);

            assertEquals(14, result.getDayOfMonth());
            assertEquals(0, result.getHour());

            ZonedDateTime friday = ZonedDateTime.of(2024, 6, 14, 12, 0, 0, 0, UTC);
            result = FinancialTimeFrameAligner.alignFloor(friday, TimeFrame.WK);
            assertEquals(6, result.getMonthValue());
            assertEquals(14, result.getDayOfMonth());
            assertEquals(0, result.getHour());

            friday = ZonedDateTime.of(2024, 6, 14, 0, 0, 0, 0, UTC);
            result = FinancialTimeFrameAligner.alignFloor(friday, TimeFrame.WK);
            assertEquals(6, result.getMonthValue());
            assertEquals(14, result.getDayOfMonth());
            assertEquals(0, result.getHour());

            friday = ZonedDateTime.of(2024, 6, 13, 23, 59, 59, 59, UTC);
            result = FinancialTimeFrameAligner.alignFloor(friday, TimeFrame.WK);
            assertEquals(6, result.getMonthValue());
            assertEquals(7, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Dimanche (16 Juin) -> Semaine clôturée -> Renvoie Vendredi 14 Juin 00:00")
        void testClosedWeekSunday() {
            ZonedDateTime sunday = ZonedDateTime.of(2024, 6, 16, 22, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(sunday, TimeFrame.WK);

            assertEquals(14, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Semaine avec Noël (Mercredi 25 Décembre 2024) -> Semaine clôturée normalement le Vendredi 17 Décembre")
        void testChristmasWeek() {
            // Samedi 28 Décembre 2024
            ZonedDateTime saturdayAfterChristmas = ZonedDateTime.of(2024, 12, 27, 11, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(saturdayAfterChristmas, TimeFrame.WK);

            assertEquals(12, result.getMonthValue());
            assertEquals(27, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Semaine clôturée le Vendredi car férié (Vendredi 25 Décembre 2026) -> Clôture avancée au Vendredi 25 Décembre 00:00")
        void testWeekEndingOnFridayHoliday() {
            // Le 25 Décembre 2026 est un Vendredi (férié fixe dans TimeUtils).
            // Interrogation le Samedi 26 Décembre 2026 (après la clôture de la semaine)
            ZonedDateTime saturdayAfterFridayHoliday = ZonedDateTime.of(2026, 12, 26, 14, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(saturdayAfterFridayHoliday, TimeFrame.WK);

            // Comme le Vendredi 25 est férié, la semaine de cotation s'arrête le Jeudi 24 à 23:59:59.
            // getEndOfLastBusinessDay(saturday) recule jusqu'au Jeudi 24 Décembre 00:00:00.
            assertEquals(2026, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(24, result.getDayOfMonth());
            assertEquals(0, result.getHour());

            ZonedDateTime fridayAfterFridayHoliday = ZonedDateTime.of(2026, 12, 25, 14, 0, 0, 0, UTC);
            result = FinancialTimeFrameAligner.alignFloor(fridayAfterFridayHoliday, TimeFrame.WK);
            assertEquals(24, result.getDayOfMonth());
            assertEquals(0, result.getHour());

        }
    }

    // =========================================================================
    // 4. TIMEFRAME MENSUEL (MO)
    // =========================================================================
    @Nested
    @DisplayName("Alignement Mensuel (MO)")
    class MonthlyTests {

        @Test
        @DisplayName("En cours de mois (15 Juin 2024) -> Clôture du mois précédent (Samedi 1er Juin 00:00)")
        void testMidMonthToPreviousMonth() {
            ZonedDateTime midMonth = ZonedDateTime.of(2024, 6, 15, 14, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(midMonth, TimeFrame.MO);

            // 31 Mai était Vendredi ouvré. Fin du mois de Mai = Vendredi 31 Mai 00:00:00
            assertEquals(5, result.getMonthValue());
            assertEquals(31, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Fin de mois tombant un Dimanche (30 Juin 2024) -> Mois clôturé le Vendredi 28 Juin 00:00")
        void testMonthEndingOnWeekend() {
            ZonedDateTime sundayEnd = ZonedDateTime.of(2024, 6, 30, 18, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(sundayEnd, TimeFrame.MO);

            assertEquals(6, result.getMonthValue());
            assertEquals(28, result.getDayOfMonth()); // Vendredi 28 Juin 00:00:00
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Fin de mois tombant un jour férié fixe (25 Décembre 2024 - Mercredi)")
        void testMonthEndingWithHoliday() {
            // Le 25 Décembre est férié fixe dans TimeUtils.
            // On interroge pendant le mois de Décembre (ex: 26 Décembre)
            ZonedDateTime dec26 = ZonedDateTime.of(2024, 12, 26, 10, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(dec26, TimeFrame.MO);

            // Décembre n'est pas fini -> clôture de Novembre (Novembre finit le Vendredi 29 Novembre)
            assertEquals(11, result.getMonthValue());
            assertEquals(29, result.getDayOfMonth());
        }

        @Test
        @DisplayName("Passage d'année (15 Janvier 2024) -> Clôture Décembre 2023 (Vendredi 29 Décembre 00:00)")
        void testYearTransition() {
            ZonedDateTime january = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(january, TimeFrame.MO);

            // 29 Décembre 2023 était un Vendredi. Fin de Décembre = Vendredi 29 Décembre 00:00:00
            assertEquals(2023, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(29, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Février bissextile (29 Fév 2024 Jeudi) -> Interrogé le 1er Mars -> 29 Fév 2024 Jeudi 00:00")
        void testLeapYearFebruary() {
            ZonedDateTime marchFirst = ZonedDateTime.of(2024, 3, 1, 8, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(marchFirst, TimeFrame.MO);

            // 29 Février 2024 = Jeudi. Clôture de Février = Vendredi 1er Mars 00:00:00
            assertEquals(2, result.getMonthValue());
            assertEquals(29, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }

        @Test
        @DisplayName("Février non bissextile (28 Fév 2023 Mardi) -> Interrogé le 1er Mars -> Mardi 28 Fevrier 00:00")
        void testNonLeapYearFebruary() {
            ZonedDateTime marchFirst = ZonedDateTime.of(2023, 3, 1, 8, 0, 0, 0, UTC);
            ZonedDateTime result = FinancialTimeFrameAligner.alignFloor(marchFirst, TimeFrame.MO);

            // 28 Février 2023 = Mardi. Clôture de Février = Mercredi 1er Mars 00:00:00
            assertEquals(2, result.getMonthValue());
            assertEquals(28, result.getDayOfMonth());
            assertEquals(0, result.getHour());
        }
    }
}