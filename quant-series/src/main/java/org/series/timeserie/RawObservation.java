package org.series.timeserie;

import lombok.Getter;

import java.time.ZoneId;


public class RawObservation implements Observation {

    @Getter
    private final long dateTime;

    @Getter
    private final double doubleValue;

    @Getter
    private final ZoneId zoneId;

    public RawObservation(long dateTime, double value, ZoneId zoneId) {
        this.dateTime = dateTime;
        this.doubleValue = value;
        this.zoneId = zoneId;

    }

    @Override
    public int compareTo(RawObservation o) {
        return Double.compare(this.doubleValue, o.getDoubleValue());
    }
}
