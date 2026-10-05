package org.statistics.estimator.parametric.model;

import org.math.vector.Vector;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;

public class NormalModel implements ParametricModel<NormalDistribution> {

    private static final double LN_SQRT_2PI = 0.5 * Math.log(2.0 * Math.PI);

    @Override
    public boolean isValidParameterSet(Vector params) {
        if (params == null || params.size() != 2) {
            return false;
        }
        double mu = params.getValue(0);
        double sigma = params.getValue(1);

        return !Double.isNaN(mu) && !Double.isInfinite(mu)
                && sigma > 0.0 && !Double.isNaN(sigma) && !Double.isInfinite(sigma);
    }

    @Override
    public double logLikelihood(double x, Vector params) {
        if (!isValidParameterSet(params)) {
            return Double.NEGATIVE_INFINITY;
        }
        double mu = params.getValue(0);
        double sigma = params.getValue(1);

        double z = (x - mu) / sigma;
        return -LN_SQRT_2PI - Math.log(sigma) - 0.5 * z * z;
    }

    @Override
    public double mean(Vector theta) {
        return theta.getValue(0);
    }

    @Override
    public double variance(Vector theta) {
        double sigma = theta.getValue(1);
        return sigma * sigma;
    }

    @Override
    public NormalDistribution createDistribution(Vector params) {
        if (!isValidParameterSet(params)) {
            throw new IllegalArgumentException("Invalid parameter vector for NormalDistribution: " + params);
        }
        return new NormalDistribution(params.getValue(0), params.getValue(1));
    }
}