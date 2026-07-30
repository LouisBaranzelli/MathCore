package org.data.definitions.history;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Predicate;

public final class TradingBuisnessDayUtil {

    private TradingBuisnessDayUtil() {
    }

    public static boolean isBusinessDay(ZonedDateTime dt) {
        return isBusinessDay(dt, TradingBuisnessDayUtil::isFixedHoliday);
    }

    public static boolean isBusinessDay(ZonedDateTime dt, Predicate<ZonedDateTime> holidayPredicate) {
        Objects.requireNonNull(dt);
        return isWeekday(dt) && !holidayPredicate.test(dt);
    }

    public static boolean isWeekday(ZonedDateTime dt) {
        DayOfWeek dow = dt.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    public static boolean isFixedHoliday(ZonedDateTime dt) {
        Month month = dt.getMonth();
        int day = dt.getDayOfMonth();
        return (month == Month.JANUARY && day == 1)
                || (month == Month.MAY && day == 1)
                || (month == Month.DECEMBER && day == 25);
    }

    public static boolean isLastBusinessDayOfMonth(ZonedDateTime dt) {
        return isLastBusinessDayOfMonth(dt, TradingBuisnessDayUtil::isFixedHoliday);
    }

    public static boolean isLastBusinessDayOfMonth(ZonedDateTime dt, Predicate<ZonedDateTime> holidayPredicate) {
        if (!isBusinessDay(dt, holidayPredicate)) {
            return false;
        }
        int lastDay = dt.toLocalDate().lengthOfMonth();
        for (int day = dt.getDayOfMonth() + 1; day <= lastDay; day++) {
            ZonedDateTime futureDate = dt.withDayOfMonth(day);
            if (isBusinessDay(futureDate, holidayPredicate)) {
                return false;
            }
        }
        return true;
    }
}