package org.estimator;


import org.statistics.probability.distributions.monovariate.EmpiricalDistribution;

import java.util.function.DoubleUnaryOperator;

/**
 * Représente une bande de confiance non-paramétrique pour une fonction de répartition continue F(x).
 * <p>
 * Définit un tuyau de sécurité [L(x), U(x)] encadrant la vraie fonction de répartition
 * avec une probabilité minimale garantie par {@code confidenceLevel}.
 *
 * @param lowerBand La fonction borne inférieure L(x) = max(F_n(x) - epsilon, 0)
 * @param upperBand La fonction borne supérieure U(x) = min(F_n(x) + epsilon, 1)
 * @param epsilon La demi-largeur maximale théorique de la bande (marge d'erreur DKW)
 * @param confidenceLevel Le niveau de confiance global 1 - alpha (ex: 0.95 pour 95%)
 */

public record EmpiricalDistributionConfidenceBand(
        DoubleUnaryOperator lowerBand,
        DoubleUnaryOperator upperBand,
        double epsilon,
        double confidenceLevel
) {
    public boolean containsDistributionFunction(DoubleUnaryOperator f, double[] testPoints) {
        for (double x : testPoints) {
            double fx = f.applyAsDouble(x);
            if (fx < lowerBand.applyAsDouble(x) || fx > upperBand.applyAsDouble(x)) {
                return false;
            }
        }
        return true;
    }
}