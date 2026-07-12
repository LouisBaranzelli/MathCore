package org.series;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;

public final class ImmutableDoubleTimeSerie implements DoubleTimeSerie {

    private final long[] timestamps;
    private final double[] values;

    public ImmutableDoubleTimeSerie(long[] timestamps, double[] values) {

        validate(timestamps, values);
        this.timestamps = timestamps.clone();
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
        return timestamps[index];
    }

    @Override
    public Vector toVector() {
        double[] copyNonRecurssive = new double[values.length];
        for (int i=0; i < values.length; i++){
            copyNonRecurssive[i] =getValue(i);
        }
        return new ArrayVector(copyNonRecurssive);
    }

    private static void validate(long[] timestamps, double[] values) {
        if (timestamps == null || values == null) {
            throw new IllegalArgumentException("Arrays cannot be null");
        }
        if (timestamps.length != values.length) {
            throw new IllegalArgumentException("Mismatched lengths: " + timestamps.length + " vs " + values.length);
        }
        if (timestamps.length == 0) {
            throw new IllegalArgumentException("Time serie cannot be empty");
        }

        for (int i = 0; i < timestamps.length; i++) {
            if (Double.isNaN(values[i]) || Double.isInfinite(values[i])) {
                throw new IllegalArgumentException("Invalid numeric value (NaN/Infinite) at index " + i);
            }
            if (i > 0 && timestamps[i] <= timestamps[i - 1]) {
                throw new IllegalArgumentException("Timestamps must be strictly increasing. Violation at index " + i);
            }
        }
    }
}
