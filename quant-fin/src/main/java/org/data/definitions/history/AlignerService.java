package org.data.definitions.history;

import org.data.definitions.assets.Instrument;
import org.series.timegrid.TimeFrameAligner;

import java.time.DayOfWeek;

public class AlignerService {

    public static TimeFrameAligner get(Instrument instrument){
        return new StandartFinancialBuisnessDayAligner(DayOfWeek.FRIDAY);
    }
}
