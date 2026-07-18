package org.series;

import lombok.Getter;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;

public enum TimeFrame {

    MI("Mi", "1 Minute", 1, ZonedDateTime::minusMinutes, "yyyy-MM-dd HH:mm"),
    MI5("Mi5", "5 Minutes", 5, ZonedDateTime::minusMinutes, "yyyy-MM-dd HH:mm"),
    MI15("Mi15", "15 Minutes", 15, ZonedDateTime::minusMinutes, "yyyy-MM-dd HH:mm"),
    MI30("Mi30", "30 Minutes", 30, ZonedDateTime::minusMinutes, "yyyy-MM-dd HH:mm"),
    HR("Hr", "1 Hour", 1, ZonedDateTime::minusHours, "yyyy-MM-dd HH:mm"),
    D("D1", "1 Day", 1, ZonedDateTime::minusDays, "yyyy-MM-dd"),
    WK("Wk", "1 Week", 1, ZonedDateTime::minusWeeks, "yyyy-MM-dd"),
    MO("Mo", "1 Month", 1, ZonedDateTime::minusMonths, "yyyy-MM");

    @Getter
    private final String code;

    @Getter
    private final String label;

    @Getter
    private final String stringFormat;

    private final long stepAmount;
    private final BiFunction<ZonedDateTime, Long, ZonedDateTime> minusOperation;

    TimeFrame(String code, String label, long stepAmount,
              BiFunction<ZonedDateTime, Long, ZonedDateTime> minusOperation, String stringFormat) {
        this.code = code;
        this.label = label;
        this.stepAmount = stepAmount;
        this.minusOperation = minusOperation;
        this.stringFormat = stringFormat;
    }

    /**
     * Recule proprement d'un pas de temps complet.
     * Exemple : pour MI5, appliquera minusMinutes(date, 5)
     */
    public ZonedDateTime shiftBackward(ZonedDateTime dateTime) {
        return this.minusOperation.apply(dateTime, this.stepAmount);
    }

}