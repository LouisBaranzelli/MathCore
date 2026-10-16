package org.statistics.estimator.parametric.model;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;

/**
 * Modèle paramétrique pour la loi normale (Gaussienne).
 * Appartient au module statistique/inférence.
 */
public class NormalModel implements ParametricModel<NormalDistribution> {

    private static final Logger log = LoggerFactory.getLogger(NormalModel.class);

    // Constante : 0.5 * ln(2 * PI)
    private static final double HALF_LN_2PI = 0.5 * Math.log(2.0 * Math.PI);

    @Override
    public boolean isValidParameterSet(Vector params) {
        if (params == null || params.size() != 2) {
            log.debug("Invalid parameter vector size: expected 2 (mu, sigma), got {}", params == null ? "null" : params.size());
            return false;
        }
        double mu = params.getValue(0);
        double sigma = params.getValue(1);

        boolean valid = !Double.isNaN(mu) && !Double.isInfinite(mu)
                && sigma > 0.0 && !Double.isNaN(sigma) && !Double.isInfinite(sigma);

        if (!valid) {
            log.warn("Invalid parameter values detected: mu={}, sigma={}", mu, sigma);
        }
        return valid;
    }

    @Override
    public double logLikelihood(double x, Vector params) {
        if (!isValidParameterSet(params)) {
            return Double.NEGATIVE_INFINITY;
        }
        double mu = params.getValue(0);
        double sigma = params.getValue(1);

        double z = (x - mu) / sigma;
        // ln(pdf) = -ln(sigma) - 0.5*ln(2*pi) - 0.5*z^2
        return -Math.log(sigma) - HALF_LN_2PI - 0.5 * z * z;
    }

    @Override
    public double mean(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for mean calculation.");
        }
        return theta.getValue(0);
    }

    @Override
    public double variance(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for variance calculation.");
        }
        double sigma = theta.getValue(1);
        return sigma * sigma;
    }

    @Override
    public NormalDistribution createDistribution(Vector params) {
        if (!isValidParameterSet(params)) {
            log.error("Attempted to create NormalDistribution with invalid parameters: {}", params);
            throw new IllegalArgumentException("Invalid parameter vector for NormalDistribution: " + params);
        }
        log.debug("Creating NormalDistribution with mu={}, sigma={}", params.getValue(0), params.getValue(1));
        return new NormalDistribution(params.getValue(0), params.getValue(1));
    }
}