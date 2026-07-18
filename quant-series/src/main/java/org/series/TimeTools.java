package org.series;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimeTools {

    public static long fromInstantToLong(Instant instant){
        return instant.getEpochSecond();
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


}
