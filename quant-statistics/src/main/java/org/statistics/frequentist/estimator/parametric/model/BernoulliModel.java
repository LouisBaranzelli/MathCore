package org.statistics.frequentist.estimator.parametric.model;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.discrete.BernoulliDistribution;

/**
 * Modèle paramétrique pour la loi de Bernoulli (événement binaire).
 * Appartient au module statistique/inférence, avec un espace de paramètres
 * à une seule dimension : p ∈ [0, 1].
 */
public class BernoulliModel implements ParametricModel<BernoulliDistribution> {

    private static final Logger log = LoggerFactory.getLogger(BernoulliModel.class);

    @Override
    public boolean isValidParameterSet(Vector params) {
        if (params == null || params.size() != 1) {
            log.debug("Invalid parameter vector size: expected 1 (p), got {}", params == null ? "null" : params.size());
            return false;
        }
        double p = params.getValue(0);

        // Le paramètre de succès p d'une loi de Bernoulli doit être strictement compris entre 0 et 1 (ou inclus selon les conventions,
        // mais pour le calcul de log-vraisemblance p=0 ou p=1 pose problème avec les log, on restreint souvent à ]0, 1[ ou [0, 1]).
        boolean valid = !Double.isNaN(p) && !Double.isInfinite(p) && p >= 0.0 && p <= 1.0;

        if (!valid) {
            log.warn("Invalid parameter value detected for Bernoulli p: p={}", p);
        }
        return valid;
    }

    @Override
    public double logLikelihood(double x, Vector params) {
        if (!isValidParameterSet(params)) {
            return Double.NEGATIVE_INFINITY;
        }
        double p = params.getValue(0);

        if (x != 0.0 && x != 1.0) {
            log.debug("Observation x={} is not a valid binary outcome (expected 0.0 or 1.0)", x);
            return Double.NEGATIVE_INFINITY;
        }

        if (p == 0.0) {
            return (x == 0.0) ? 0.0 : Double.NEGATIVE_INFINITY;
        }
        if (p == 1.0) {
            return (x == 1.0) ? 0.0 : Double.NEGATIVE_INFINITY;
        }

        return x * Math.log(p) + (1.0 - x) * Math.log(1.0 - p);
    }

    @Override
    public double mean(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for Bernoulli mean calculation.");
        }
        return theta.getValue(0);
    }

    @Override
    public double variance(Vector theta) {
        if (!isValidParameterSet(theta)) {
            throw new IllegalArgumentException("Invalid parameters for Bernoulli variance calculation.");
        }
        double p = theta.getValue(0);
        return p * (1.0 - p);
    }

    @Override
    public BernoulliDistribution createDistribution(Vector params) {
        if (!isValidParameterSet(params)) {
            log.error("Attempted to create BernoulliDistribution with invalid parameters: {}", params);
            throw new IllegalArgumentException("Invalid parameter vector for BernoulliDistribution: " + params);
        }
        log.debug("Creating BernoulliDistribution with p={}", params.getValue(0));
        return new BernoulliDistribution(params.getValue(0));
    }
}