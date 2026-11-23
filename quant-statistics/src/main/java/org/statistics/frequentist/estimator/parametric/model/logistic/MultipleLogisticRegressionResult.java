package org.statistics.frequentist.estimator.parametric.model.logistic;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.multivariate.CovarianceMatrix;

/**
 * Résultat immuable d'une régression logistique multiple estimée par maximum de vraisemblance (IRLS).
 */
public record MultipleLogisticRegressionResult(
        Vector coefficients,
        double logLikelihood,
        CovarianceMatrix asymptoticCovariance,
        Vector fittedProbabilities,
        int iterations,
        boolean converged
) {
    private static final Logger log = LoggerFactory.getLogger(MultipleLogisticRegressionResult.class);

    public MultipleLogisticRegressionResult {
        if (coefficients == null) {
            throw new IllegalArgumentException("Coefficients vector must not be null.");
        }
        if (asymptoticCovariance == null) {
            throw new IllegalArgumentException("Asymptotic covariance matrix must not be null.");
        }
        if (fittedProbabilities == null) {
            throw new IllegalArgumentException("Fitted probabilities vector must not be null.");
        }
        if (coefficients.size() < 1) {
            throw new IllegalArgumentException("Coefficients vector must contain at least one element.");
        }
        if (iterations < 0) {
            throw new IllegalArgumentException("Iterations count must be non-negative.");
        }
    }

    /**
     * Retourne l'erreur standard asymptotique du j-ième coefficient.
     */
    public double standardError(int index) {
        log.trace("standardError called with index={}", index);
        int k = coefficients.size();
        if (index < 0 || index >= k) {
            throw new IllegalArgumentException("Coefficient index out of bounds: " + index);
        }
        double variance = asymptoticCovariance.get(index, index);
        if (variance < 0.0 || Double.isNaN(variance) || Double.isInfinite(variance)) {
            throw new IllegalStateException("Invalid variance at index " + index + ": " + variance);
        }
        double se = Math.sqrt(variance);
        log.debug("standardError completed for index={}: se={}", index, se);
        return se;
    }
}