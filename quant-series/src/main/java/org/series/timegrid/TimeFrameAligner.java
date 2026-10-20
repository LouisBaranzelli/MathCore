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

    default ZonedDateTime alignCeil(ZonedDateTime dateTime, TimeFrame timeFrame) {

                ZonedDateTime floor = alignFloor(dateTime, timeFrame);
                ZonedDateTime truncated = dateTime.truncatedTo(ChronoUnit.SECONDS);
                if (floor.equals(truncated)) {
                    return floor;
                }

                ZonedDateTime ceil;

                if (timeFrame.equals(TimeFrame.D) || timeFrame.equals(TimeFrame.WK)){
                   ceil = ensureValidDayGoingForward(floor.plusDays(1));
                   /*
                   pour des raison de perfs je ne diminue pas le step à moins de 15 min
                   a condition que les premières heures valides ne soient pas batardes (ex 9h05)
                   */
                   ceil = ensureValidHourGoingForward(ceil, 15 * 60);
                } else {
                    int minutesAmout = (int) (timeFrame == TimeFrame.HR ? 60 : timeFrame.getStepAmount());
                    ceil = floor.plusMinutes(minutesAmout);
                    if (!isValidHour(ceil)){
                        //trop tard dans une journée valide
                        ceil = ceil.toLocalDate().plusDays(1).atStartOfDay(ceil.getZone());
                        ceil = ensureValidDayGoingForward(ceil);
                        ceil = ensureValidHourGoingForward(ceil, (int) timeFrame.getStepAmount() * 60);
                    }

                }
                return ceil;
    }


    private ZonedDateTime alignMinute(ZonedDateTime dt, int interval) {
        if (!isValidDay(dt)) {
            return ensureValidHourGoingBack(ensureValidDayGoingBack(dt).plusDays(1).minusSeconds(interval), interval);
        }
        int minute = dt.getMinute();
        int intervalMinute = interval /60;
        int targetMinute = (minute / intervalMinute) * intervalMinute;
        return ensureValidHourGoingBack(dt.withMinute(targetMinute).withSecond(0).withNano(0), interval);
    }

    private  ZonedDateTime alignHour(ZonedDateTime dt) {
        if (!isValidDay(dt)) {
            return ensureValidHourGoingBack(ensureValidDayGoingBack(dt).plusDays(1).minusHours(1), 3600);
        }
        return ensureValidHourGoingBack(dt.withMinute(0).withSecond(0).withNano(0), 3600);
    }

    private ZonedDateTime alignDay(ZonedDateTime dt) {

        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        if (!isValidDay(dt)) {
            return ensureValidDayGoingBack(dt);
        }
        return midnight;
    }

    private ZonedDateTime alignWeek(ZonedDateTime dt) {
        ZonedDateTime midnight = dt.with(LocalTime.MIDNIGHT);
        ZonedDateTime nextOrSameEndOfWeekMidnight = midnight.with(TemporalAdjusters.nextOrSame(getLastDayOfWeek()));
        ZonedDateTime endOfCurrentWeek = ensureValidDayGoingBack(nextOrSameEndOfWeekMidnight);

        if (!midnight.isBefore(endOfCurrentWeek)) {
            return endOfCurrentWeek;
        }
        ZonedDateTime endOfPreviousWeek = nextOrSameEndOfWeekMidnight.minusWeeks(1);
        return ensureValidDayGoingBack(endOfPreviousWeek);
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
    default ZonedDateTime ensureValidDayGoingBack(ZonedDateTime dt){
        ZonedDateTime current = dt;
        while (!isValidDay(current)) {
            current = current.minusDays(1).with(LocalTime.MIDNIGHT);
        }
        return current;
    }

    default ZonedDateTime ensureValidDayGoingForward(ZonedDateTime dt){
        ZonedDateTime current = dt;
        while (!isValidDay(current)) {
            current = current.toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(current.getZone());
        }
        return current;
    }

    default ZonedDateTime ensureValidHourGoingBack(ZonedDateTime dt, int seconds){
        ZonedDateTime current = dt;
        while (!isValidHour(current)) {
            current = current.minusSeconds(seconds);
            if (!isValidDay(current)){
                current = ensureValidDayGoingBack(current).plusDays(1).minusSeconds(seconds);
            }
        }
        return current;
    }

    default ZonedDateTime ensureValidHourGoingForward(ZonedDateTime dt, int seconds){
        ZonedDateTime current = dt;
        while (!isValidHour(current)) {
            current = current.plusSeconds(seconds);
            if (!isValidDay(current)){
                current = ensureValidDayGoingForward(current);
            }
        }
        return current;
    }

    boolean isValidHour(ZonedDateTime dt);

}