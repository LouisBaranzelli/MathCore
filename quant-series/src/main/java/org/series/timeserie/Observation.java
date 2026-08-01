package org.series.timeserie;

import java.time.ZonedDateTime;

public interface Observation extends Comparable<RawObservation> {
    double getDoubleValue();

    long getDateTime();
}
