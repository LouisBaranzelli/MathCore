package org.series;

import org.series.timeserie.TimeFrame;

import java.time.*;

public class TimeTools {

    public static long fromInstantToLong(Instant instant){
        return instant.getEpochSecond();
    }

    public static long fromDurationToLong(Duration duration) {
        Instant instantFromDuration = Instant.EPOCH.plus(duration);
        return fromInstantToLong(instantFromDuration);
    }

    public static Instant fromLongToInstant(long longValue){
        return Instant.ofEpochSecond(longValue);
    }

    public static ZonedDateTime fromLongToZonedDateTime(long longValue, ZoneId zoneId){
        return ZonedDateTime.ofInstant(fromLongToInstant(longValue), zoneId);
    }

    public static long fromZonedDateTimeToLong(ZonedDateTime zone){
        return zone.toInstant().getEpochSecond() ;
    }

    public static long fromZonedDateTimeToIndex(ZonedDateTime zone){
        return zone.toInstant().getEpochSecond() ;
    }

    public static int getNumberValuesStartingFromEndBetween(long start, long end, TimeFrame timeframe){
        if (start > end){
            long tmp = end;
            end = start;
            start = tmp;
        }
        long delta = TimeTools.fromDurationToLong(timeframe.getDuration());
        long i = end;
        int size = 1;
        while (i - delta >= start){
            i = i - delta;
            size++;
        }
        return size;
    }

    public static long fromDayStringToLong(String day, ZoneIdEnum zoneIdEnum){
        LocalDate localDate = LocalDate.parse(day);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
        ZonedDateTime targetZonedDateTime = ZonedDateTime.of(localDateTime, zoneIdEnum.getZoneId());
        return TimeTools.fromZonedDateTimeToLong(targetZonedDateTime);
    }



}
