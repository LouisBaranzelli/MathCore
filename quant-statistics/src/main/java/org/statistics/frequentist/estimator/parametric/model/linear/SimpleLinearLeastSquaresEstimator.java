package org.statistics.frequentist.estimator.parametric.model.linear;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.descriptive.DescriptiveStatistics;
import org.statistics.multivariate.CovarianceMatrix;
import org.statistics.multivariate.DenseCovarianceMatrix;

/**
 * Estimateur par Moindres Carrés Ordinaires (OLS) pour la régression linéaire simple.
 */
public class SimpleLinearLeastSquaresEstimator {

    private static final Logger log = LoggerFactory.getLogger(SimpleLinearLeastSquaresEstimator.class);
    private static final double EPSILON = 1e-12;

    /**
     * Estime le vecteur des coefficients [beta_0, beta_1].
     */
    public Vector estimate(Vector x, Vector y) {
        log.trace("estimate called with x={}, y={}", x, y);
        Vector coefficients = fit(x, y).coefficients();
        log.debug("estimate completed with coefficients={}", coefficients);
        return coefficients;
    }

    /**
     * Ajuste le modèle linéaire simple et produit les quantités nécessaires à l'inférence.
     */
    public SimpleLinearRegressionResult fit(Vector x, Vector y) {
        log.trace("fit called with x={}, y={}", x, y);
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

        if (sxx <= EPSILON) {
            log.warn("Predictor X has near-zero variance (sxx={}); regression slope is undefined.", sxx);
            throw new IllegalArgumentException("Predictor X has zero variance; cannot fit linear regression.");
        }

        double beta1 = sxy / sxx;
        double beta0 = meanY - beta1 * meanX;

        double[] residualArray = new double[n];
        double rss = 0.0;
        for (int i = 0; i < n; i++) {
            double residual = y.getValue(i) - (beta0 + beta1 * x.getValue(i));
            residualArray[i] = residual;
            rss += residual * residual;
        }

        double sigmaSquared = rss / (n - 2);
        double sigma = Math.sqrt(sigmaSquared);

        double varBeta1 = sigmaSquared / sxx;
        double varBeta0 = varBeta1 * (sumXSquared / n);
        double covBeta0Beta1 = -meanX * varBeta1;

        CovarianceMatrix covarianceMatrix = new DenseCovarianceMatrix(new double[][]{
                {varBeta0, covBeta0Beta1},
                {covBeta0Beta1, varBeta1}
        });

        double rSquared = (syy <= EPSILON) ? Double.NaN : 1.0 - (rss / syy);
        Vector residuals = new ArrayVector(residualArray);

        SimpleLinearRegressionResult result = new SimpleLinearRegressionResult(
                beta0, beta1, sigma, covarianceMatrix, residuals, rSquared, n
        );

        log.debug("fit completed: beta0={}, beta1={}, sigma={}, rSquared={}", beta0, beta1, sigma, rSquared);
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
            throw new IllegalArgumentException("At least 3 observations are required to fit simple linear regression.");
        }
    }
}