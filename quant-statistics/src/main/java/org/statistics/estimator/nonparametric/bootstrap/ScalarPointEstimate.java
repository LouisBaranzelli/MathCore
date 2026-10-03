package org.statistics.estimator.nonparametric.bootstrap;

import org.math.matrix.Matrix;
import org.math.matrix.MatrixFactory;

public record ScalarPointEstimate(
        Double value,
        double variance,
        long sampleSize
) implements PointEstimate<Double> {

    public ScalarPointEstimate {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be strictly positive.");
        }
        if (variance < 0.0) {
            throw new IllegalArgumentException("Variance cannot be negative.");
        }
    }

    public Matrix covarianceMatrix() {
        return MatrixFactory.scalar(variance);
    }
}