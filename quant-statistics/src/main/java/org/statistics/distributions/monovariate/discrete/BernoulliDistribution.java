package org.statistics.distributions.monovariate.discrete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modélise la loi de Bernoulli de paramètre p (probabilité de succès),
 * où p ∈ [0, 1].
 */
public class BernoulliDistribution implements DiscreteDistribution {

    private static final Logger logger = LoggerFactory.getLogger(BernoulliDistribution.class);
    private final double p;

    /**
     * Crée une nouvelle distribution de Bernoulli.
     *
     * @param p la probabilité de succès, comprise entre 0.0 et 1.0 (inclus)
     */
    public BernoulliDistribution(double p) {
        if (p < 0.0 || p > 1.0) {
            logger.error("Invalid p parameter: {}. Parameter p must be between 0.0 and 1.0.", p);
            throw new IllegalArgumentException("Le paramètre p doit être compris entre 0.0 et 1.0.");
        }
        this.p = p;
    }


    public double getP() {
        return p;
    }

    @Override
    public double pmf(int k) {
        if (k == 0) {
            return 1.0 - p;
        } else if (k == 1) {
            return p;
        } else {
            return 0.0;
        }
    }

    @Override
    public double cdf(double x) {
        if (x < 0.0) {
            return 0.0;
        } else if (x >= 0.0 && x < 1.0) {
            return 1.0 - p;
        } else {
            return 1.0;
        }
    }
}