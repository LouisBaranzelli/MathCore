package org.series;

import org.math.vector.Vector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    default LocalDateTime getLocalDateTime(int index, ZoneId zoneId){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getTimestamp(index)), zoneId);
    }


}
