package org.statistics.estimator.parametric.model;

import org.math.probability.distribution.ChiSquaredDistribution;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modèle paramétrique pour la loi du Chi-deux (Chi-Squared Model).
 * Appartient au module statistique/inférence.
 */
public class ChiSquaredModel implements ParametricModel<ChiSquaredDistribution> {

    private static final Logger log = LoggerFactory.getLogger(ChiSquaredModel.class);

    @Override
    public boolean isValidParameterSet(Vector params) {
        if (params == null || params.size() != 1) {
            log.debug("Invalid parameter vector size: expected 1 (degrees of freedom), got {}", params == null ? "null" : params.size());
            return false;
        }
        double kDouble = params.getValue(0);

        // Le degré de liberté doit être un entier positif
        boolean valid = !Double.isNaN(kDouble) && !Double.isInfinite(kDouble)
                && kDouble > 0.0 && kDouble == Math.floor(kDouble);

        if (!valid) {
            log.warn("Invalid parameter value for ChiSquared degrees of freedom: k={}", kDouble);
        }
        return valid;
    }

    @Override
    public double logLikelihood(double x, Vector params) {
        if (!isValidParameterSet(params)) {
            return Double.NEGATIVE_INFINITY;
        }
        // Délègue au calcul de la densité sous-jacente de la distribution
        int k = (int) params.getValue(0);
        ChiSquaredDistribution dist = new ChiSquaredDistribution(k);
        double density = dist.density(x);

        if (density <= 0.0) {
            return Double.NEGATIVE_INFINITY;
        }
        return Math.log(density);
    }

    @Override
    public double mean(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for mean calculation.");
        }
        // L'espérance d'une loi du Chi-deux à k degrés de liberté est k
        return theta.getValue(0);
    }

    @Override
    public double variance(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for variance calculation.");
        }
        double k = theta.getValue(0);
        // La variance d'une loi du Chi-deux à k degrés de liberté est 2k
        return 2.0 * k;
    }

    @Override
    public ChiSquaredDistribution createDistribution(Vector params) {
        if (!isValidParameterSet(params)) {
            log.error("Attempted to create ChiSquaredDistribution with invalid parameters: {}", params);
            throw new IllegalArgumentException("Invalid parameter vector for ChiSquaredDistribution: " + params);
        }
        int k = (int) params.getValue(0);
        log.debug("Creating ChiSquaredDistribution with k={}", k);
        return new ChiSquaredDistribution(k);
    }
}