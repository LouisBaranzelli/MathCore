package org.statistics.estimator.nonparametric.test;

import java.util.Map;

public record GoodnessOfFitResult(int totalObservations, double chiSquareStatistic, int degreesOfFreedom,
                                  double pValue, Map<String, Double> categoryContributions){}

