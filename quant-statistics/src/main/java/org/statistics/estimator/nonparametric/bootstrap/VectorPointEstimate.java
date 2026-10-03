package org.statistics.estimator.nonparametric.bootstrap;

import org.math.matrix.Matrix;
import org.math.vector.Vector;

public record VectorPointEstimate(
        Vector value,
        Matrix covarianceMatrix,
        long sampleSize
) implements PointEstimate<Vector> {

    public VectorPointEstimate {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be strictly positive.");
        }
    }
}