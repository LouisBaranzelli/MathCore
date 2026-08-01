package org.series.timeserie;

import java.time.ZonedDateTime;

public class StubObservation implements Observation {
    private final long dateTime;
    private final double value;

    public StubObservation(long dateTime, double value) {
        this.dateTime = dateTime;
        this.value = value;
    }

    @Override
    public long getDateTime() {
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
