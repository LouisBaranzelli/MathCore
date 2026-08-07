package org.data.definitions.history;

import org.series.timeserie.TimeFrame;

import java.time.ZonedDateTime;
import java.util.Objects;

public class BuisnessDay implements TimeGridPredicate{

    @Override
    public boolean test(ZonedDateTime dateTime, TimeFrame timeFrame) {
        return Objects.equals(dateTime, FinancialTimeFrameAligner.alignFloor(dateTime, timeFrame));
    }

}
