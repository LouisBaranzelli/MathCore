package org.series.timegrid;

import org.series.TimeFrame;
import org.series.TimeTools;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

public final class TimeGridFactory {

    private TimeGridFactory() {}


    public static TimeGrid create
            (ZonedDateTime end, int size, Predicate<ZonedDateTime> validDate, TimeFrame timeFrame) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be strictly positive");
        }

        long[] dates = new long[size];
        int index = size - 1;
        ZonedDateTime currentDateIndex = end;

        int maxAttempts = size * 20;
        int attempts = 0;

        while (index >= 0) {
            if (attempts++ > maxAttempts) {
                throw new IllegalStateException(
                        String.format("Grid generation aborted: local timeout. Too many dates rejected by the predicate. Remaining slots: %d", index + 1)
                );
            }
            if (validDate.test(currentDateIndex)) {
                dates[index] = TimeTools.fromZonedDateTimeToLong(currentDateIndex);
                index--;
            }
            currentDateIndex = timeFrame.shiftBackward(currentDateIndex);
        }

        return new IrregularTimeGrid(dates);
    }
}