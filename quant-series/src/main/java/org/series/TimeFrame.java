package org.series;

import lombok.Getter;

import java.time.Duration;

public enum TimeFrame {

    MI("Mi", "1 Minute", Duration.ofMinutes(1), "yyyy-MM-dd HH:mm"),
    MI5("Mi5", "5 Minutes", Duration.ofMinutes(5), "yyyy-MM-dd HH:mm"),
    MI15("Mi15", "15 Minutes", Duration.ofMinutes(15), "yyyy-MM-dd HH:mm"),
    MI30("Mi30", "30 Minutes", Duration.ofMinutes(30), "yyyy-MM-dd HH:mm"),
    HR("Hr", "1 Hour", Duration.ofMinutes(60), "yyyy-MM-dd HH:mm"),
    D("D1", "1 Day", Duration.ofMinutes(1440), "yyyy-MM-dd"),
    WK("Wk", "1 Week", Duration.ofMinutes(10080), "yyyy-MM-dd"),
    MO("Mo", "1 Month", Duration.ofMinutes(43200), "yyyy-MM");

    @Getter
    private final String code;

    @Getter
    private final String label;

    @Getter
    private final Duration deltaMinutes;

    @Getter
    private final String stringFormat;

    TimeFrame(String code, String label, Duration deltaMinutes, String stringFormat) {
        this.code = code;
        this.label = label;
        this.deltaMinutes = deltaMinutes;
        this.stringFormat = stringFormat;
    }
}