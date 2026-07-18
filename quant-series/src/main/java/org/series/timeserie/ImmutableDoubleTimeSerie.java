package org.series.timeserie;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;

public final class ImmutableDoubleTimeSerie implements DoubleTimeSerie {

    private final TimeGrid timeGrid;
    private final double[] values;

    public ImmutableDoubleTimeSerie(TimeGrid timeGrid, double[] values) throws InvalidTimeSerieException {

        validate(timeGrid, values);
        this.timeGrid = timeGrid;
        this.values = values.clone();
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public double getValue(int index) {
        return values[index];
    }

    @Override
    public long getTimestamp(int index) {
        return timeGrid.getTimeStamp(index);
    }

    @Override
    public Vector toVector() {
        double[] copyNonRecurssive = new double[values.length];
        for (int i=0; i < values.length; i++){
            copyNonRecurssive[i] =getValue(i);
        }
        return new ArrayVector(copyNonRecurssive);
    }

    private static void validate(TimeGrid timeGrid, double[] values) throws  InvalidTimeSerieException {
        if (values == null) {
            throw new IllegalArgumentException("Arrays cannot be null");
        }
        if (timeGrid.size() != values.length) {
            throw new IllegalArgumentException("Mismatched lengths: " + timeGrid.size() + " vs " + values.length);
        }

        for (int i = 0; i < timeGrid.size(); i++) {
            if (Double.isNaN(values[i]) || Double.isInfinite(values[i])) {
                throw new InvalidTimeSerieException("Invalid numeric value (NaN/Infinite) at index " + i);
            }
        }
    }
}


