package org.series;

import org.math.vector.Vector;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface DoubleTimeSerie {

    int size();

    double getValue(int index);

    long getTimestamp(int index);

    default Instant getInstant(int index) {
        return Instant.ofEpochMilli(getTimestamp(index));
    }

    Vector toVector();

    default Instant getStart() {
        return Instant.ofEpochMilli(getTimestamp(0));
    }

    default Instant getEnd() {
        return Instant.ofEpochMilli(getTimestamp(size() - 1));
    }

    default ZonedDateTime getZoneDateTime(int index, ZoneId zoneId){
        return TimeTools.fromLongToZonedDateTime(getTimestamp(index), zoneId);
    }


}
