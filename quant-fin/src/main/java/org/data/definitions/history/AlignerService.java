package org.data.definitions.history;

import org.data.definitions.assets.Instrument;
import org.series.TimeTools;
import org.series.timegrid.TimeFrameAligner;
import org.series.timeserie.TimeFrame;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Predicate;

public class AlignerService {

    public static TimeFrameAligner get(Instrument instrument){
        return new StandartFinancialBuisnessDayAligner(DayOfWeek.FRIDAY);
    }

    public static Predicate<Long> getValidDatePredicateBasedOnAligner(Instrument instrument, TimeFrame timeFrame){
        ZoneId zoneId = instrument.getZoneIdEnum().getZoneId();
        TimeFrameAligner timeFrameAligner = AlignerService.get(instrument);
        return (Long date) -> Objects.equals(
                TimeTools.fromLongToZonedDateTime(date, zoneId), timeFrameAligner.alignFloor(TimeTools.fromLongToZonedDateTime(date, zoneId), timeFrame)
        );
    }
}
