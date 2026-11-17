package org.statistics.frequentist.estimator.parametric.model;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.descriptive.DescriptiveStatistics;

/**
 * Estimateur par Moindres Carrés Ordinaires (OLS) pour la régression linéaire simple.
 */
public class SimpleLinearLeastSquaresEstimator {

    private static final Logger log = LoggerFactory.getLogger(SimpleLinearLeastSquaresEstimator.class);

    /**
     * Estime uniquement le vecteur de paramètres [beta_0, beta_1, sigma].
     */
    public Vector estimate(Vector x, Vector y) {
        return fit(x, y).getParameters();
    }

    /**
     * Ajuste la régression linéaire simple et retourne l'objet complet d'inférence
     * (paramètres, erreurs standard homoscédastiques, résidus et R^2).
     *
     * @param x le vecteur des variables explicatives (covariables)
     * @param y le vecteur des variables réponses
     * @return le résultat complet de la régression
     */
    public SimpleLinearRegressionResult fit(Vector x, Vector y) {
        log.trace("fit called with x size: {}, y size: {}",
                x == null ? "null" : x.size(), y == null ? "null" : y.size());

        validateInputs(x, y);

        int n = x.size();
        double meanX = DescriptiveStatistics.mean(x);
        double meanY = DescriptiveStatistics.mean(y);

        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;
        double sumXSquared = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = x.getValue(i);
            double yi = y.getValue(i);
            double dx = xi - meanX;
            double dy = yi - meanY;

            sxx += dx * dx;
            syy += dy * dy;
            sxy += dx * dy;
            sumXSquared += xi * xi;
        }

        if (sxx == 0.0) {
            log.error("Variance of predictor X is zero; regression slope is undefined.");
            throw new IllegalArgumentException("Predictor X has zero variance; cannot fit linear regression.");
        }

        double beta1 = sxy / sxx;
        double beta0 = meanY - beta1 * meanX;

        // Calcul des résidus et de la somme des carrés des résidus (RSS)
        double[] residualArray = new double[n];
        double rss = 0.0;
        for (int i = 0; i < n; i++) {
            double predicted = beta0 + beta1 * x.getValue(i);
            double residual = y.getValue(i) - predicted;
            residualArray[i] = residual;
            rss += residual * residual;
        }

        // Estimateur sans biais de l'écart-type des résidus (n - 2 degrés de liberté)
        double sigma = Math.sqrt(rss / (n - 2));

        // Erreurs standard sous hypothèse d'homoscédasticité
        double seBeta1 = sigma / Math.sqrt(sxx);
        double seBeta0 = seBeta1 * Math.sqrt(sumXSquared / n);

        // Coefficient de détermination R^2
        double rSquared = (syy == 0.0) ? 1.0 : 1.0 - (rss / syy);

        Vector residuals = new ArrayVector(residualArray);
        SimpleLinearRegressionResult result = new SimpleLinearRegressionResult(
                beta0, beta1, sigma, seBeta0, seBeta1, residuals, rSquared
        );

        log.debug("fit completed: beta_0={} (se={}), beta_1={} (se={}), sigma={}, R^2={}",
                beta0, seBeta0, beta1, seBeta1, sigma, rSquared);

        return result;
    }

    private void validateInputs(Vector x, Vector y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Input vectors x and y must not be null.");
        }
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Vectors x and y must have the same length.");
        }
        if (x.size() < 3) {
            throw new IllegalArgumentException("At least 3 observations are required to estimate 2 parameters and residual variance.");
        }
    }
}