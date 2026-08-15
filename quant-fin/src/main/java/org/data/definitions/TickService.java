package org.data.definitions;

import org.series.timeserie.TimeFrame;

import java.util.List;

public class TickService {

    /**
     * En fonction de l'enum, la base de temps est différent. Une journée de trade peut de finir à 16h ou 17h
     * en fonction du pays. On peut généraliser en didant que ca finit au même moment si le tick est à la journée.
     * @param timeFrame
     * @return
     */
    public static TickEnum getTick(TimeFrame timeFrame){
        List<TimeFrame> daysRelative = List.of(TimeFrame.D, TimeFrame.WK);
        List<TimeFrame> secondRelative = List.of(TimeFrame.HR, TimeFrame.MI30, TimeFrame.MI15, TimeFrame.MI5, TimeFrame.MI);

        if (daysRelative.contains(timeFrame)){
            return TickEnum.DAY;
        }

        if (secondRelative.contains(timeFrame)){
            return TickEnum.SECOND;
        }
        throw new RuntimeException(timeFrame.getLabel() + " not taken in charge.");
    }
}
