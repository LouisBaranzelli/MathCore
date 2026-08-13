package org.series;

import org.series.timeserie.TimeFrame;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Predicate;

public final class TimeTools {

    public static final Duration ONE_SECOND = Duration.ofSeconds(1);

    private TimeTools() {
    }

    public static long fromInstantToLong(Instant instant) {
        return Objects.requireNonNull(instant).getEpochSecond();
    }

    public static long fromDurationToLong(Duration duration) {
        return Objects.requireNonNull(duration).toSeconds();
    }

    public static Instant fromLongToInstant(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds);
    }

    public static ZonedDateTime fromLongToZonedDateTime(long epochSeconds, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(fromLongToInstant(epochSeconds), zoneId);
    }

    public static long fromZonedDateTimeToLong(ZonedDateTime zonedDateTime) {
        return Objects.requireNonNull(zonedDateTime).toInstant().getEpochSecond();
    }

    public static long fromZonedDateTimeToIndex(ZonedDateTime zonedDateTime) {
        return fromZonedDateTimeToLong(zonedDateTime);
    }

    public static int getNumberValuesStartingFromEndBetween(long start, long end, TimeFrame timeFrame, Predicate<Long> validDate, ZoneId zoneId) {
        int size = getNumberValuesStartingFromEndBetween(start, end, timeFrame, zoneId);
        int unValidDates = 0;
        for (long i=end; i>=start ;i=getMinusPeriod(i, timeFrame, zoneId)){
            if (!validDate.test(i)){
                unValidDates++;
            }
        }
        if (size != 0 && size - unValidDates == 0){
            throw new RuntimeException(
                    "The predicate for valid dates and available dates is completely out of sync"
            );        }
        return size - unValidDates;
    }

    private static long getMinusPeriod(long i, TimeFrame timeFrame, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = TimeTools.fromLongToZonedDateTime(i, zoneId);
        ZonedDateTime previousZonedDateTime = null;
        switch (timeFrame){
            case MI -> previousZonedDateTime = zonedDateTime.minusMinutes(1);
            case MI5 -> previousZonedDateTime = zonedDateTime.minusMinutes(5);
            case MI15 -> previousZonedDateTime = zonedDateTime.minusMinutes(15);
            case MI30 -> previousZonedDateTime = zonedDateTime.minusMinutes(30);
            case HR -> previousZonedDateTime = zonedDateTime.minusHours(1);
            case D -> previousZonedDateTime = zonedDateTime.minusDays(1);
            case WK -> previousZonedDateTime = zonedDateTime.minusDays(7);
        }
        return TimeTools.fromZonedDateTimeToLong(previousZonedDateTime);
    }

    public static int getNumberValuesStartingFromEndBetween(long startSeconds, long endSeconds, TimeFrame timeFrame, ZoneId zoneId) {
        ZonedDateTime start = TimeTools.fromLongToZonedDateTime(Math.min(startSeconds, endSeconds), zoneId);
        ZonedDateTime end =  TimeTools.fromLongToZonedDateTime(Math.max(startSeconds, endSeconds), zoneId);

        long deltaSeconds = fromDurationToLong(timeFrame.getDuration());
        if (deltaSeconds <= 0) {
            throw new IllegalArgumentException("TimeFrame duration must be positive");
        }
        long seconds = Duration.between(start, end).toSeconds();
        return (int) (seconds / deltaSeconds) + 1;
    }

    public static long fromDayStringToLong(String dayString, ZoneIdEnum zoneIdEnum) {
        LocalDate localDate = LocalDate.parse(dayString);
        LocalDateTime localDateTime = localDate.atStartOfDay();
        ZonedDateTime targetZonedDateTime = ZonedDateTime.of(localDateTime, zoneIdEnum.getZoneId());
        return fromZonedDateTimeToLong(targetZonedDateTime);
    }

    /**
     * ex "2023-02-24T15:30:00"
     * @param dateTimeString
     * @param zoneIdEnum
     * @return
     */
    public static long fromDateTimeStringToLong(String dateTimeString, ZoneIdEnum zoneIdEnum) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString);
        ZonedDateTime targetZonedDateTime = ZonedDateTime.of(localDateTime, zoneIdEnum.getZoneId());
        return fromZonedDateTimeToLong(targetZonedDateTime);
    }

    public static Duration getOneTick() {
        return ONE_SECOND;
    }

    public static ZonedDateTime truncate(ZonedDateTime dt) {
        return dt.truncatedTo(ChronoUnit.SECONDS);
    }
}