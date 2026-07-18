package org.series.timegrid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TimeGridFactoryTest {

    // Prédicat standard : accepte tous les jours
    private final Predicate<ZonedDateTime> acceptAll = date -> true;

    // Prédicat réaliste : uniquement les jours de la semaine (Lundi au Vendredi)
    private final Predicate<ZonedDateTime> businessDaysOnly = date ->
            date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;

    @Test
    @DisplayName("Should generate continuous timeline when all dates are valid")
    void shouldGenerateContinuousTimeline() {
        // GIVEN
        ZonedDateTime end = ZonedDateTime.of(2026, 7, 17, 17, 0, 0, 0, ZoneOffset.UTC); // Un vendredi
        int size = 5;

        // WHEN
        TimeGrid grid = TimeGridFactory.create(end, size, acceptAll, TimeFrame.D);

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
        ZonedDateTime monday = ZonedDateTime.of(2026, 7, 20, 17, 0, 0, 0, ZoneOffset.UTC);
        int size = 2; // On veut Lundi et le jour ouvré précédent (Vendredi)

        // WHEN
        TimeGrid grid = TimeGridFactory.create(monday, size, businessDaysOnly, TimeFrame.D);

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
                TimeGridFactory.create(end, 0, acceptAll, TimeFrame.D)
        );
        assertEquals("Size must be strictly positive", ex1.getMessage());

        Exception ex2 = assertThrows(IllegalArgumentException.class, () ->
                TimeGridFactory.create(end, -5, acceptAll, TimeFrame.D)
        );
        assertEquals("Size must be strictly positive", ex2.getMessage());
    }

    @Test
    @DisplayName("Should trigger local timeout and throw IllegalStateException if predicate rejects too many dates")
    void shouldTimeoutWhenPredicateIsTooRestrictive() {
        // GIVEN
        ZonedDateTime end = ZonedDateTime.now(ZoneOffset.UTC);
        Predicate<ZonedDateTime> rejectAll = date -> false; // Rejette absolument tout

        // WHEN & THEN
        Exception ex = assertThrows(IllegalStateException.class, () ->
                TimeGridFactory.create(end, 5, rejectAll, TimeFrame.D)
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
        TimeGrid grid = TimeGridFactory.create(end, size, acceptAll, TimeFrame.MI5);

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