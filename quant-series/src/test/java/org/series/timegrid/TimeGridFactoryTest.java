package org.series.timegrid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.timeserie.TimeFrame;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TimeGridFactoryTest {

    // Prédicat standard : accepte tous les jours
    private final Predicate<Long> acceptAll = date -> true;

    // Prédicat réaliste : uniquement les jours de la semaine (Lundi au Vendredi)
    private final Predicate<Long> businessDaysOnly = date ->
            TimeTools.fromLongToZonedDateTime(date, ZoneIdEnum.EUROPE_PARIS.getZoneId()).getDayOfWeek() != DayOfWeek.SATURDAY && TimeTools.fromLongToZonedDateTime(date, ZoneIdEnum.EUROPE_PARIS.getZoneId()).getDayOfWeek() != DayOfWeek.SUNDAY;

    @Test
    @DisplayName("Should generate continuous timeline when all dates are valid")
    void shouldGenerateContinuousTimeline() {
        // GIVEN
        ZonedDateTime end = ZonedDateTime.of(2026, 7, 17, 17, 0, 0, 0, ZoneOffset.UTC); // Un vendredi
        int size = 5;

        // WHEN
        TimeGrid grid = TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(end), size, acceptAll, TimeFrame.D);

        // THEN
        assertEquals(5, grid.size());

        // On vérifie que la dernière date (index 4) est bien la date de fin
        long expectedEndLong = TimeTools.fromZonedDateTimeToLong(end);
        assertEquals(expectedEndLong, grid.getTimeStamp(4));

        // On vérifie que les dates sont bien strictement croissantes dans le tableau
        assertTrue(grid.getTimeStamp(1) > grid.getTimeStamp(0));
        assertTrue(grid.getTimeStamp(2) > grid.getTimeStamp(1));
        assertTrue(grid.getTimeStamp(3) > grid.getTimeStamp(2));
        assertTrue(grid.getTimeStamp(4) > grid.getTimeStamp(3));
    }

    @Test
    @DisplayName("Should skip weekends correctly using business days predicate")
    void shouldSkipWeekendsCorrectly() {
        // GIVEN
        // Lundi 20 Juillet 2026
        ZonedDateTime monday = ZonedDateTime.of(2026, 7, 20, 17, 0, 0, 0, ZoneIdEnum.EUROPE_PARIS.getZoneId());
        int size = 2; // On veut Lundi et le jour ouvré précédent (Vendredi)

        // WHEN
        TimeGrid grid = TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(monday), size, businessDaysOnly, TimeFrame.D);

        // THEN
        assertEquals(2, grid.size());

        // Index 1 doit être le Lundi
        assertEquals(TimeTools.fromZonedDateTimeToLong(monday), grid.getTimeStamp(1));

        // Index 0 doit être le Vendredi 17 Juillet (Samedi 18 et Dimanche 19 ont été sautés)
        ZonedDateTime expectedFriday = monday.minusDays(3);
        assertEquals(TimeTools.fromZonedDateTimeToLong(expectedFriday), grid.getTimeStamp(0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when size is zero or negative")
    void shouldThrowExceptionWhenSizeIsInvalid() {
        ZonedDateTime end = ZonedDateTime.now(ZoneOffset.UTC);

        Exception ex1 = assertThrows(IllegalArgumentException.class, () ->
                TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(end), 0, acceptAll, TimeFrame.D)
        );
        assertEquals("Size must be strictly positive", ex1.getMessage());

        Exception ex2 = assertThrows(IllegalArgumentException.class, () ->
                TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(end), -5, acceptAll, TimeFrame.D)
        );
        assertEquals("Size must be strictly positive", ex2.getMessage());
    }

    @Test
    @DisplayName("Should trigger local timeout and throw IllegalStateException if predicate rejects too many dates")
    void shouldTimeoutWhenPredicateIsTooRestrictive() {
        // GIVEN
        ZonedDateTime end = ZonedDateTime.now(ZoneOffset.UTC);
        Predicate<Long> rejectAll = date -> false; // Rejette absolument tout

        // WHEN & THEN
        Exception ex = assertThrows(IllegalStateException.class, () ->
                TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(end), 5, rejectAll, TimeFrame.D)
        );
        assertTrue(ex.getMessage().contains("Grid generation aborted: local timeout"));
    }

    @Test
    @DisplayName("Should support intra-day timeframes like MI5")
    void shouldSupportIntradayTimeFrames() {
        // GIVEN
        ZonedDateTime end = ZonedDateTime.of(2026, 7, 17, 12, 0, 0, 0, ZoneOffset.UTC);
        int size = 3;

        // WHEN
        TimeGrid grid = TimeGridFactory.create(TimeTools.fromZonedDateTimeToLong(end), size, acceptAll, TimeFrame.MI5);

        // THEN
        assertEquals(3, grid.size());

        long t2 = grid.getTimeStamp(2); // 12:00
        long t1 = grid.getTimeStamp(1); // 11:55
        long t0 = grid.getTimeStamp(0); // 11:50

        // Vérification des écarts de 5 minutes (5 * 60 secondes = 300)
        assertEquals(300, t2 - t1);
        assertEquals(300, t1 - t0);
    }

}

class TimeGridFactoryBoundedCreateTest {

    private final Predicate<Long> acceptAll = date -> true;

    private final Predicate<Long> businessDaysOnly = date -> {
        DayOfWeek day = TimeTools.fromLongToZonedDateTime(date, ZoneIdEnum.EUROPE_PARIS.getZoneId()).getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    };

    @Test
    @DisplayName("Should create grid with exact size between start and end when all dates are valid")
    void shouldCreateGridBetweenStartAndEnd() {
        // GIVEN : Intervalle de 10:00 à 11:00 en pas de 15 min -> 5 points (10:00, 10:15, 10:30, 10:45, 11:00)
        long start = Instant.parse("2026-07-17T10:00:00Z").getEpochSecond();
        long end = Instant.parse("2026-07-17T11:00:00Z").getEpochSecond();

        // WHEN
        TimeGrid grid = TimeGridFactory.create(start, end, acceptAll, TimeFrame.MI15);

        // THEN
        assertEquals(5, grid.size());
        assertEquals(start, grid.getTimeStamp(0));
        assertEquals(end, grid.getTimeStamp(4));
        assertEquals(start + 900, grid.getTimeStamp(1)); // +15 min (900s)
    }

    @Test
    @DisplayName("Should create grid excluding weekends between start and end date")
    void shouldCreateGridExcludingWeekends() {
        // GIVEN : Du Vendredi 17 Juillet 2026 au Lundi 20 Juillet 2026 en pas de 1 jour (D1)
        long friday = Instant.parse("2026-07-17T17:00:00Z").getEpochSecond();
        long monday = Instant.parse("2026-07-20T17:00:00Z").getEpochSecond();

        // WHEN
        TimeGrid grid = TimeGridFactory.create(friday, monday, businessDaysOnly, TimeFrame.D);

        // THEN : Seuls Vendredi et Lundi sont valides -> size = 2
        assertEquals(2, grid.size());
        assertEquals(friday, grid.getTimeStamp(0));
        assertEquals(monday, grid.getTimeStamp(1));
    }

    @Test
    @DisplayName("Should create grid excluding weekends between start and end date")
    void shouldCreateGridExcludingWeekendsNotRound() {
        // GIVEN : Du Vendredi 17 Juillet 2026 au Lundi 20 Juillet 2026 en pas de 1 jour (D1)
        long fridayMinusOneHour = Instant.parse("2026-07-17T16:00:00Z").getEpochSecond();
        long friday = Instant.parse("2026-07-17T17:00:00Z").getEpochSecond();
        long monday = Instant.parse("2026-07-20T17:00:00Z").getEpochSecond();

        // WHEN
        TimeGrid grid = TimeGridFactory.create(fridayMinusOneHour, monday, businessDaysOnly, TimeFrame.D);

        // THEN : Seuls Vendredi et Lundi sont valides -> size = 2
        assertEquals(2, grid.size());
        assertEquals(friday, grid.getTimeStamp(0));
        assertEquals(monday, grid.getTimeStamp(1));
    }

    @Test
    @DisplayName("Should create single element grid when start equals end and date is valid")
    void shouldCreateSingleElementGridWhenStartEqualsEnd() {
        // GIVEN
        long timestamp = Instant.parse("2026-07-17T12:00:00Z").getEpochSecond();

        // WHEN
        TimeGrid grid = TimeGridFactory.create(timestamp, timestamp, acceptAll, TimeFrame.HR);

        // THEN
        assertEquals(1, grid.size());
        assertEquals(timestamp, grid.getTimeStamp(0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when size evaluates to zero (start > end)")
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        // GIVEN : start est après end
        long start = Instant.parse("2026-07-17T12:00:00Z").getEpochSecond();
        long end = Instant.parse("2026-07-17T10:00:00Z").getEpochSecond();

        // WHEN & THEN
        // getNumberValuesStartingFromEndBetween renvoie 0 -> create(end, 0, ...) lève IllegalArgumentException
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TimeGridFactory.create(start, end, acceptAll, TimeFrame.MI5)
        );
        assertEquals("start must be before the end, got start: 1784289600 and end: 1784282400", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no dates are valid in interval")
    void shouldThrowExceptionWhenNoDatesAreValid() {
        // GIVEN
        long start = Instant.parse("2026-07-17T10:00:00Z").getEpochSecond();
        long end = Instant.parse("2026-07-17T11:00:00Z").getEpochSecond();
        Predicate<Long> rejectAll = date -> false;

        // WHEN & THEN
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TimeGridFactory.create(start, end, rejectAll, TimeFrame.MI15)
        );
    }
}