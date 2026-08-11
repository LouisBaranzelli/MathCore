package org.series.timeserie;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface Observation extends Comparable<RawObservation> {
    double getDoubleValue();

    long getDateTime();

    ZoneId getZoneId();
}
