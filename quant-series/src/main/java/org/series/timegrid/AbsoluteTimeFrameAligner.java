package org.series.timegrid;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;


public class AbsoluteTimeFrameAligner implements TimeFrameAligner {

    @Getter
    private final DayOfWeek lastDayOfWeek;

    public AbsoluteTimeFrameAligner(DayOfWeek endOfWeek) {
        this.lastDayOfWeek = endOfWeek;
    }

    @Override
    public boolean isValidDay(ZonedDateTime dt){
       return true;
    }

    @Override
    public ZonedDateTime ensureValidDay(ZonedDateTime dt) {
        return dt.with(LocalTime.MIDNIGHT);
    }

    @Override
    public boolean isValidHour(ZonedDateTime dt) {
        return true;
    }
}