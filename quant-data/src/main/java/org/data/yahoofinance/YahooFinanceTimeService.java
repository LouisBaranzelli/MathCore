package org.data.yahoofinance;

import org.series.TimeTools;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class YahooFinanceTimeService {

    public static long setHourAt(int hour, long datetime, ZoneId zoneId){
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(datetime, zoneId);
        zonedDateTime = zonedDateTime.withHour(hour).withMinute(0).withMinute(0).withSecond(0).withNano(0);
        return TimeTools.fromZonedDateTimeToLong(zonedDateTime);
    }

    public static long setAtFriday(long datetime, ZoneId zoneId){
        datetime = setHourAt(0, datetime,zoneId);
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(datetime, zoneId);
        if (zonedDateTime.getDayOfWeek().equals(DayOfWeek.FRIDAY)){
            return datetime;
        }
        zonedDateTime = zonedDateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        return TimeTools.fromZonedDateTimeToLong(zonedDateTime);
    }

    public static long setMidnightNextDay(long datetime, ZoneId zoneId){
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(datetime, zoneId);
        zonedDateTime = zonedDateTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        return TimeTools.fromZonedDateTimeToLong(zonedDateTime);
    }

}
