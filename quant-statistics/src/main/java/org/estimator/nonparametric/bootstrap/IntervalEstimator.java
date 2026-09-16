package org.estimator.nonparametric.bootstrap;

public interface IntervalEstimator <T, S>{

    ConfidenceInterval<T> estimateInterval(S sample, double confidenceLevel);}
