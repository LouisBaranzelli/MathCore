package org.estimator.parametric.model;

import org.estimator.parametric.ParametricEstimator;
import org.estimator.parametric.moment.EmpiricalMomentEstimator;
import org.math.vector.Vector;
import org.distributions.monovariate.continuous.NormalDistribution;

public class NormalDistributionParametricEstimator implements ParametricEstimator<NormalDistribution> {

    @Override
    public NormalDistribution fit(Vector data) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("At least two data points are required for estimation.");
        }

        double m1 = EmpiricalMomentEstimator.calculateRawMoment(data, 1);
        double m2 = EmpiricalMomentEstimator.calculateRawMoment(data, 2);

        double variance = m2 - (m1 * m1);

        if (variance <= 0.0) {
            throw new IllegalArgumentException("Sample variance must be strictly positive to estimate sigma.");
        }

        double sigma = Math.sqrt(variance);
        return new NormalDistribution(m1, sigma);
    }

}
