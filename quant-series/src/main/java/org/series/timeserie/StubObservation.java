package org.series.timeserie;

import java.time.ZonedDateTime;

public class StubObservation implements Observation {
    private final ZonedDateTime dateTime;
    private final double value;

    public StubObservation(ZonedDateTime dateTime, double value) {
        this.dateTime = dateTime;
        this.value = value;
    }

    @Override
    public ZonedDateTime getZonedDateTime() {
        return dateTime;
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public int compareTo(RawObservation o) {
        return 0;
    }
}
