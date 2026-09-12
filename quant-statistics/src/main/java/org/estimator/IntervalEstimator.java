package org.estimator;

public interface IntervalEstimator <T, S>{

    ConfidenceInterval<T> estimateInterval(S sample, double confidenceLevel);}
