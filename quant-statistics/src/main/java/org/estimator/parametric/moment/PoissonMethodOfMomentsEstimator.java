package org.estimator.parametric.moment;

import org.math.vector.Vector;
import org.statistics.probability.distributions.monovariate.PoissonDistribution;

public final class PoissonMethodOfMomentsEstimator implements ParametricEstimator<PoissonDistribution> {

    @Override
    public PoissonDistribution fit(Vector data) {
        if (data == null || data.size() < 1) {
            throw new IllegalArgumentException("At least one data point is required for estimation.");
        }

        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (data.getValue(i) < 0.0) {
                throw new IllegalArgumentException("Poisson distribution requires non-negative observations.");
            }
        }

        double sampleMean = EmpiricalMomentEstimator.calculateRawMoment(data, 1);

        if (sampleMean <= 0.0) {
            throw new IllegalArgumentException("Sample mean must be strictly positive to estimate lambda.");
        }

        return new PoissonDistribution(sampleMean);
    }
}