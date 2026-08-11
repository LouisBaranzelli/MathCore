package org.series.timegrid;

import org.series.timeserie.TimeFrame;
import org.series.TimeTools;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Predicate;

public final class TimeGridFactory {

    private TimeGridFactory() {}


    public static TimeGrid create
            (long end, int size, Predicate<Long> validDate, TimeFrame timeFrame) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be strictly positive");
        }

        long[] dates = new long[size];
        int index = size - 1;
        long currentDateIndex = end;

        int maxAttempts = size * 20;
        int attempts = 0;

        while (index >= 0) {
            if (attempts++ > maxAttempts) {
                throw new IllegalStateException(
                        String.format("Grid generation aborted: local timeout. Too many dates rejected by the predicate. Remaining slots: %d", index + 1)
                );
            }
            if (validDate.test(currentDateIndex)) {
                dates[index] = currentDateIndex;
                index--;
            }
            currentDateIndex = currentDateIndex - TimeTools.fromDurationToLong(timeFrame.getDuration());
        }

        return new IrregularTimeGrid(dates);
    }

    public static TimeGrid create
            (long start, long end, Predicate<Long> validDate, TimeFrame timeFrame, ZoneId zoneId) {
        if (start > end){
            throw new IllegalArgumentException(String.format("start must be before the end, got start: %s and end: %s", start, end));
        }


        int size = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame, validDate, zoneId); // start included
        return TimeGridFactory.create(end, size, validDate, timeFrame);
    }

}