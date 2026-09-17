package org.estimator.parametric.moment;

import org.math.vector.Vector;

public final class EmpiricalMomentEstimator {


    public static double calculateRawMoment(Vector data, int k) {
        if (k < 1) {
            throw new IllegalArgumentException("Moment order k must be strictly positive.");
        }
        double sum = 0.0;
        for (int i=0; i< data.size() ; i++) {
            sum += Math.pow(data.getValue(i), k);
        }
        return sum / data.size();
    }
}