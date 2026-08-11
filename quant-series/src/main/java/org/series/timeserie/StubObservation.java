package org.series.timeserie;

import lombok.Getter;
import org.series.ZoneIdEnum;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class StubObservation implements Observation {
    private final long dateTime;
    private final double value;

    @Getter
    private final ZoneId zoneId;

    public StubObservation(long dateTime, double value) {
        this.dateTime = dateTime;
        this.value = value;
        this.zoneId = ZoneIdEnum.UTC.getZoneId();
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
