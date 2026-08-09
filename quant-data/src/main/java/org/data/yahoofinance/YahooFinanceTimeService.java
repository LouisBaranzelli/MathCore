package org.data.yahoofinance;

import org.series.TimeTools;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class YahooFinanceTimeService {

    public static long setHourAt(int hour, long datetime, ZoneId zoneId){
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(datetime, zoneId);
        zonedDateTime = zonedDateTime.withHour(hour).withMinute(0).withMinute(0).withSecond(0).withNano(0);
        return TimeTools.fromZonedDateTimeToLong(zonedDateTime);
    }

    public static long setMidnightNextDay(long datetime, ZoneId zoneId){
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(datetime, zoneId);
        zonedDateTime = zonedDateTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        return TimeTools.fromZonedDateTimeToLong(zonedDateTime);
    }

}
