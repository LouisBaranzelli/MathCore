package org.data.definitions.history;

import org.series.timeserie.TimeFrame;

import java.time.ZonedDateTime;

public interface TimeGridPredicate {

    public boolean test(ZonedDateTime dateTime, TimeFrame timeFrame);
}
