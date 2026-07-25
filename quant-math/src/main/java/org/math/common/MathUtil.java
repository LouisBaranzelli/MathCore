package org.math.common;

public class MathUtil {

    public static double round(double value, int decimals) {
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals must be non-negative");
        }
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
