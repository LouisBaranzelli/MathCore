package org.series.timegrid;

import org.series.timeserie.TimeFrame;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


public interface TimeFrameAligner {

    default ZonedDateTime alignFloor(ZonedDateTime dateTime, TimeFrame timeFrame) {
        ZonedDateTime truncated = dateTime.truncatedTo(ChronoUnit.SECONDS);

        return switch (timeFrame) {
            case MI  -> this.alignMinute(truncated, 60);
            case MI5 -> this.alignMinute(truncated, 5*60);
            case MI15-> this.alignMinute(truncated, 15*60);
            case MI30-> this.alignMinute(truncated, 30*60);
            case HR  -> this.alignHour(truncated);
            case D   -> this.alignDay(truncated);
            case WK  -> this.alignWeek(truncated);
        };
    }


    private ZonedDateTime alignMinute(ZonedDateTime dt, int interval) {
        if (!isValidDay(dt)) {
            return ensureValidHour(ensureValidDay(dt).plusDays(1).minusSeconds(interval), interval);
        }
        int minute = dt.getMinute();
        int intervalMinute = interval /60;
        int targetMinute = (minute / intervalMinute) * intervalMinute;
        return ensureValidHour(dt.withMinute(targetMinute).withSecond(0).withNano(0), interval);
    }

    private  ZonedDateTime alignHour(ZonedDateTime dt) {
        if (!isValidDay(dt)) {
            return ensureValidHour(ensureValidDay(dt).plusDays(1).minusHours(1), 3600);
        }
        return ensureValidHour(dt.withMinute(0).withSecond(0).withNano(0), 3600);
    }

    private ZonedDateTime alignDay(ZonedDateTime dt) {

        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        if (!isValidDay(dt)) {
            return ensureValidDay(dt);
        }
        return midnight;
    }

    private ZonedDateTime alignWeek(ZonedDateTime dt) {
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        ZonedDateTime nextOrSameEndOfWeekMidnight = midnight.with(TemporalAdjusters.nextOrSame(getLastDayOfWeek()));
        ZonedDateTime endOfCurrentWeek = ensureValidDay(nextOrSameEndOfWeekMidnight);

        if (!midnight.isBefore(endOfCurrentWeek)) {
            return endOfCurrentWeek;
        }
        ZonedDateTime endOfPreviousWeek = nextOrSameEndOfWeekMidnight.minusWeeks(1);
        return ensureValidDay(endOfPreviousWeek);
    }

    private ZonedDateTime alignMonth(ZonedDateTime dt) {
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        ZonedDateTime lastDayOfMonth = midnight.with(TemporalAdjusters.lastDayOfMonth());
        ZonedDateTime endOfLastBizDayCurrentMonth = ensureValidDay(lastDayOfMonth);
        if (!midnight.isBefore(endOfLastBizDayCurrentMonth)) {
            return endOfLastBizDayCurrentMonth;
        }
        ZonedDateTime lastDayOfPreviousMonth = midnight.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return ensureValidDay(lastDayOfPreviousMonth);
    }

    default DayOfWeek getLastDayOfWeek(){
        return DayOfWeek.SUNDAY;
    }

    boolean isValidDay(ZonedDateTime dt);
    /**
     * Retourne un jour valide à minuit.
     * @param dt
     * @return
     */
    default ZonedDateTime ensureValidDay(ZonedDateTime dt){
        ZonedDateTime current = dt;
        while (!isValidDay(current)) {
            current = current.minusDays(1).with(LocalTime.MIDNIGHT);
        }
        return current;
    }

    default ZonedDateTime ensureValidHour(ZonedDateTime dt, int seconds){
        ZonedDateTime current = dt;
        while (!isValidHour(current)) {
            current = current.minusSeconds(seconds);
            if (!isValidDay(current)){
                current = ensureValidDay(current).plusDays(1).minusSeconds(seconds);
            }
        }
        return current;
    }

    boolean isValidHour(ZonedDateTime dt);

}