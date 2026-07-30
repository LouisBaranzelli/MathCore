package org.series;

import org.series.timeserie.TimeFrame;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

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

    public static int getNumberValuesStartingFromEndBetween(long startSeconds, long endSeconds, TimeFrame timeFrame) {
        long start = Math.min(startSeconds, endSeconds);
        long end = Math.max(startSeconds, endSeconds);

        long deltaSeconds = fromDurationToLong(timeFrame.getDuration());
        if (deltaSeconds <= 0) {
            throw new IllegalArgumentException("TimeFrame duration must be positive");
        }

        return (int) ((end - start) / deltaSeconds) + 1;
    }

    public static long fromDayStringToLong(String dayString, ZoneIdEnum zoneIdEnum) {
        LocalDate localDate = LocalDate.parse(dayString);
        LocalDateTime localDateTime = localDate.atStartOfDay();
        ZonedDateTime targetZonedDateTime = ZonedDateTime.of(localDateTime, zoneIdEnum.getZoneId());
        return fromZonedDateTimeToLong(targetZonedDateTime);
    }

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