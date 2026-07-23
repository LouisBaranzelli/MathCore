package org.series.timeserie;

import org.math.vector.Vector;
import org.series.TimeTools;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface DoubleTimeSerie {

    int size();

    double getValue(int index);

    long getTimestamp(int index);

    Vector toVector();

}
