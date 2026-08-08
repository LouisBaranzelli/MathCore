package org.data.definitions.history;

import lombok.Getter;
import org.series.timegrid.TimeFrameAligner;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class StandartFinancialBuisnessDayAligner implements TimeFrameAligner {

    @Getter
    private final DayOfWeek lastDayOfWeek;

    public StandartFinancialBuisnessDayAligner(DayOfWeek endOfWeek) {
        this.lastDayOfWeek = endOfWeek;
    }

    @Override
    public boolean isValidDay(ZonedDateTime dt) {
        return TradingBuisnessDayUtil.isBusinessDay(dt);
    }

    @Override
    public boolean isValidHour(ZonedDateTime dt) {
        ZonedDateTime start = dt.withHour(9).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime end = dt.withHour(17).withMinute(0).withSecond(0).withNano(0);
        return !dt.isAfter(end) && !dt.isBefore(start);
    }
}
