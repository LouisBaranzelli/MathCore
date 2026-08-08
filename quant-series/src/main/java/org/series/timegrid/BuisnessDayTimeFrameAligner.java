package org.series.timegrid;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;


public class BuisnessDayTimeFrameAligner implements TimeFrameAligner {

    @Getter
    private final DayOfWeek lastDayOfWeek;

    public BuisnessDayTimeFrameAligner(DayOfWeek endOfWeek) {
        this.lastDayOfWeek = endOfWeek;
    }

    @Override
    public boolean isValidDay(ZonedDateTime dt) {
        DayOfWeek dow = dt.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isValidHour(ZonedDateTime dt) {
        return true;
    }
}