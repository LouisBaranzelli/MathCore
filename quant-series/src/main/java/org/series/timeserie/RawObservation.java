package org.series.timeserie;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RawObservation implements Observation {

    @Getter
    private final long dateTime;

    @Getter
    private final double doubleValue;

    public RawObservation(long dateTime, double value) {
        this.dateTime = dateTime;
        this.doubleValue = value;
    }

    @Override
    public int compareTo(RawObservation o) {
        return Double.compare(this.doubleValue, o.getDoubleValue());
    }
}
