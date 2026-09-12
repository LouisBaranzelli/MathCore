package org.estimator;

import org.math.vector.Vector;
import org.statistics.probability.definitions.Sample;
import org.statistics.probability.distributions.monovariate.EmpiricalDistribution;

/**
 * Génère une bande de confiance non-paramétrique pour la fonction de répartition F
 * en utilisant l'inégalité de Dvoretzky-Kiefer-Wolfowitz (DKW).
 */
public class DKWConfidenceBandEstimator {

    public EmpiricalDistributionConfidenceBand estimateBand(Vector sample, double confidenceLevel) {
        int n = sample.size();
        if (n < 1) {
            throw new IllegalArgumentException("L'échantillon ne peut pas être vide.");
        }
        if (confidenceLevel <= 0.0 || confidenceLevel >= 1.0) {
            throw new IllegalArgumentException("Le niveau de confiance doit être dans ]0, 1[.");
        }

        double alpha = 1.0 - confidenceLevel;
        double epsilon = Math.sqrt(Math.log(2.0 / alpha) / (2.0 * n));


        EmpiricalDistribution fn = new EmpiricalDistribution(new Sample(sample.toArray()));

        return new EmpiricalDistributionConfidenceBand(
                x -> Math.max(0.0, fn.cdf(x) - epsilon),
                x -> Math.min(1.0, fn.cdf(x) + epsilon),
                epsilon,
                confidenceLevel
        );
    }
}