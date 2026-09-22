package org.estimator.parametric.moment;

import org.estimator.parametric.ParametricEstimator;
import org.math.vector.Vector;
import org.distributions.monovariate.continuous.LogNormalDistribution;

// Located in module: statistics/inference
public final class LogNormalMethodOfMomentsEstimator implements ParametricEstimator<LogNormalDistribution> {

    @Override
    public LogNormalDistribution fit(Vector data) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("At least two data points are required.");
        }

        // 1. Calculate sample moments required by the theory
        double m1 = EmpiricalMomentEstimator.calculateRawMoment(data, 1);
        double m2 = EmpiricalMomentEstimator.calculateRawMoment(data, 2);

        if (m1 <= 0.0) {
            throw new IllegalArgumentException("Sample mean must be strictly positive for LogNormal fitting.");
        }

        double varianceOfLogs = Math.log(m2 / (m1 * m1));
        double meanOfLogs = Math.log(m1) - (0.5 * varianceOfLogs);

        double sigma = Math.sqrt(varianceOfLogs);

        return new LogNormalDistribution(meanOfLogs, sigma);
    }


}