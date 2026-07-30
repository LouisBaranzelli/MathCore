package org.data.definitions.history;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradingBuisnessDayUtilTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void testIsWeekday() {
        // Lundi 15 Janvier 2024
        ZonedDateTime monday = ZonedDateTime.of(2024, 1, 15, 10, 10, 0, 0, UTC);
        ZonedDateTime mondayMidnight = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, UTC);
        ZonedDateTime sundayMidnight = ZonedDateTime.of(2024, 1, 14, 0, 0, 0, 0, UTC);
        ZonedDateTime saturdayMidnight = ZonedDateTime.of(2024, 1, 13, 0, 0, 0, 0, UTC);
        // Samedi 20 Janvier 2024
        ZonedDateTime saturday = ZonedDateTime.of(2024, 1, 20, 10, 10, 0, 0, UTC);
        // Dimanche 21 Janvier 2024
        ZonedDateTime sunday = ZonedDateTime.of(2024, 1, 21, 10, 10, 0, 0, UTC);

        assertTrue(TradingBuisnessDayUtil.isWeekday(monday));
        assertFalse(TradingBuisnessDayUtil.isWeekday(saturday));
        assertFalse(TradingBuisnessDayUtil.isWeekday(sunday));
        assertTrue(TradingBuisnessDayUtil.isWeekday(mondayMidnight));
        assertFalse(TradingBuisnessDayUtil.isWeekday(sundayMidnight));
        assertFalse(TradingBuisnessDayUtil.isWeekday(saturdayMidnight));
    }

    @Test
    void testIsFixedHoliday() {
        ZonedDateTime newYear = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, UTC);
        ZonedDateTime laborDay = ZonedDateTime.of(2024, 5, 1, 12, 0, 0, 0, UTC);
        ZonedDateTime christmas = ZonedDateTime.of(2024, 12, 25, 23, 59, 0, 0, UTC);
        ZonedDateTime regularDay = ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, UTC);

        assertTrue(TradingBuisnessDayUtil.isFixedHoliday(newYear));
        assertTrue(TradingBuisnessDayUtil.isFixedHoliday(laborDay));
        assertTrue(TradingBuisnessDayUtil.isFixedHoliday(christmas));
        assertFalse(TradingBuisnessDayUtil.isFixedHoliday(regularDay));
    }

    @Test
    void testIsBusinessDayDefault() {
        // Lundi 1er Janvier 2024 (Férié fixe)
        ZonedDateTime mondayHoliday = ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, UTC);
        // Mardi 2 Janvier 2024 (Jour ouvré)
        ZonedDateTime tuesdayBusiness = ZonedDateTime.of(2024, 1, 2, 10, 0, 0, 0, UTC);
        // Samedi 6 Janvier 2024 (Week-end)
        ZonedDateTime saturday = ZonedDateTime.of(2024, 1, 6, 10, 0, 0, 0, UTC);

        assertFalse(TradingBuisnessDayUtil.isBusinessDay(mondayHoliday));
        assertTrue(TradingBuisnessDayUtil.isBusinessDay(tuesdayBusiness));
        assertFalse(TradingBuisnessDayUtil.isBusinessDay(saturday));
    }

    @Test
    void testIsBusinessDayCustomHoliday() {
        ZonedDateTime friday = ZonedDateTime.of(2024, 3, 29, 10, 0, 0, 0, UTC);

        // Predicate simulant Vendredi Saint (29 Mars 2024)
        Predicate<ZonedDateTime> goodFridayPredicate = dt -> dt.getMonthValue() == 3 && dt.getDayOfMonth() == 29;

        assertFalse(TradingBuisnessDayUtil.isBusinessDay(friday, goodFridayPredicate));
    }

    @Test
    void testIsLastBusinessDayOfMonth() {
        // 31 Mai 2024 est un Vendredi (Dernier jour ouvré)
        ZonedDateTime may31_2024 = ZonedDateTime.of(2024, 5, 31, 10, 0, 0, 0, UTC);
        assertTrue(TradingBuisnessDayUtil.isLastBusinessDayOfMonth(may31_2024));

        // 30 Juin 2024 est un Dimanche. Le dernier jour ouvré du mois est le Vendredi 28 Juin.
        ZonedDateTime june28_2024 = ZonedDateTime.of(2024, 6, 28, 10, 0, 0, 0, UTC);
        ZonedDateTime june30_2024 = ZonedDateTime.of(2024, 6, 30, 10, 0, 0, 0, UTC);

        assertTrue(TradingBuisnessDayUtil.isLastBusinessDayOfMonth(june28_2024));
        assertFalse(TradingBuisnessDayUtil.isLastBusinessDayOfMonth(june30_2024));
    }
}