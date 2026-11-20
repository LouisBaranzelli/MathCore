package org.statistics.frequentist.estimator.parametric.model.linear;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.multivariate.CovarianceMatrix;

/**
 * Résultat immuable d'une régression linéaire multiple par moindres carrés ordinaires.
 */
public record MultipleLinearRegressionResult(
        Vector coefficients,
        double sigma,
        CovarianceMatrix coefficientCovariance,
        Vector residuals,
        double rSquared,
        int sampleSize,
        int covariateCount
) {
    private static final Logger log = LoggerFactory.getLogger(MultipleLinearRegressionResult.class);

    public MultipleLinearRegressionResult {
        if (coefficients == null) {
            throw new IllegalArgumentException("Coefficients vector must not be null.");
        }
        if (coefficientCovariance == null) {
            throw new IllegalArgumentException("Coefficient covariance matrix must not be null.");
        }
        if (residuals == null) {
            throw new IllegalArgumentException("Residuals vector must not be null.");
        }
        if (covariateCount < 1) {
            throw new IllegalArgumentException("Covariate count must be at least 1.");
        }
        if (sampleSize <= covariateCount) {
            throw new IllegalArgumentException("Sample size must be strictly greater than covariate count.");
        }
        if (coefficients.size() != covariateCount) {
            throw new IllegalArgumentException("Coefficients size must match covariate count.");
        }
    }

    /**
     * Retourne l'erreur standard estimée du j-ième coefficient de régression.
     */
    public double standardError(int index) {
        log.trace("standardError called with index={}", index);
        if (index < 0 || index >= covariateCount) {
            throw new IllegalArgumentException("Coefficient index out of bounds: " + index);
        }
        double variance = coefficientCovariance.get(index, index);
        if (variance < 0.0) {
            throw new IllegalStateException("Negative diagonal variance at index " + index + ": " + variance);
        }
        double se = Math.sqrt(variance);
        log.debug("standardError completed for index={}: se={}", index, se);
        return se;
    }
}