package org.statistics.estimator.nonparametric.bootstrap;

import org.math.matrix.Matrix;


public interface PointEstimate <T> {

    T value();

    long sampleSize();

    Matrix covarianceMatrix();
}