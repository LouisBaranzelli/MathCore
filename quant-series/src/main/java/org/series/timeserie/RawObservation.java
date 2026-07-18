package org.series.timeserie;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RawObservation implements Observation {

    @Getter
    private final ZonedDateTime zonedDateTime;

    @Getter
    private final double doubleValue;

    public RawObservation(ZonedDateTime zonedDateTime, double value) {
        this.zonedDateTime = Objects.requireNonNull(zonedDateTime, "Instant cannot be null");
        this.doubleValue = value;
    }

    @Override
    public int compareTo(RawObservation o) {
        return Double.compare(this.doubleValue, o.getDoubleValue());
    }
}
