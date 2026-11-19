package org.statistics.frequentist.estimator.parametric.model.linear;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.multivariate.CovarianceMatrix;

/**
 * Résultat immuable d'une régression linéaire simple par moindres carrés ordinaires.
 */
public record SimpleLinearRegressionResult(
        double beta0,
        double beta1,
        double sigma,
        CovarianceMatrix coefficientCovariance,
        Vector residuals,
        double rSquared,
        int sampleSize
) {
    private static final Logger log = LoggerFactory.getLogger(SimpleLinearRegressionResult.class);

    public SimpleLinearRegressionResult {
        if (coefficientCovariance == null) {
            throw new IllegalArgumentException("Coefficient covariance matrix must not be null.");
        }
        if (residuals == null) {
            throw new IllegalArgumentException("Residuals vector must not be null.");
        }
        if (sampleSize < 3) {
            throw new IllegalArgumentException("Sample size must be at least 3.");
        }
    }

    /**
     * Retourne le vecteur des coefficients de régression [beta_0, beta_1].
     */
    public Vector coefficients() {
        log.trace("coefficients called");
        Vector result = new ArrayVector(new double[]{beta0, beta1});
        log.debug("coefficients completed with size: {}", result.size());
        return result;
    }

    /**
     * Retourne l'erreur standard estimée de l'ordonnée à l'origine beta_0.
     */
    public double beta0StandardError() {
        log.trace("beta0StandardError called");
        double se = Math.sqrt(coefficientCovariance.get(0, 0));
        log.debug("beta0StandardError completed: {}", se);
        return se;
    }

    /**
     * Retourne l'erreur standard estimée de la pente beta_1.
     */
    public double beta1StandardError() {
        log.trace("beta1StandardError called");
        double se = Math.sqrt(coefficientCovariance.get(1, 1));
        log.debug("beta1StandardError completed: {}", se);
        return se;
    }
}