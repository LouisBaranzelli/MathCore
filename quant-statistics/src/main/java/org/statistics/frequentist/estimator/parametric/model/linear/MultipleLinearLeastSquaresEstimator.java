package org.statistics.frequentist.estimator.parametric.model.linear;

import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.descriptive.DescriptiveStatistics;
import org.statistics.multivariate.CovarianceMatrix;
import org.statistics.multivariate.DenseCovarianceMatrix;

/**
 * Estimateur par Moindres Carrés Ordinaires (OLS) pour la régression linéaire multiple.
 */
public class MultipleLinearLeastSquaresEstimator {

    private static final Logger log = LoggerFactory.getLogger(MultipleLinearLeastSquaresEstimator.class);
    private static final double EPSILON = 1e-12;

    /**
     * Estime le vecteur des coefficients beta de taille k.
     */
    public Vector estimate(Matrix x, Vector y) {
        log.trace("estimate called with xRows={}, xCols={}, ySize={}",
                x == null ? -1 : x.rowCount(),
                x == null ? -1 : x.columnCount(),
                y == null ? -1 : y.size());
        Vector betaHat = fit(x, y).coefficients();
        log.debug("estimate completed with covariateCount={}", betaHat.size());
        return betaHat;
    }

    /**
     * Ajuste le modèle linéaire multiple Y = X * beta + epsilon par moindres carrés ordinaires.
     */
    public MultipleLinearRegressionResult fit(Matrix x, Vector y) {
        log.trace("fit called with xRows={}, xCols={}, ySize={}",
                x == null ? -1 : x.rowCount(),
                x == null ? -1 : x.columnCount(),
                y == null ? -1 : y.size());
        validateInputs(x, y);

        int n = x.rowCount();
        int k = x.columnCount();

        Matrix xTranspose = x.transpose();
        Matrix xTx = xTranspose.multiply(x);
        Matrix xTxInv = xTx.invert();

        Vector xTy = xTranspose.multiply(y);
        Vector betaHat = xTxInv.multiply(xTy);

        double[] residualValues = new double[n];
        double rss = 0.0;
        for (int i = 0; i < n; i++) {
            double predicted = 0.0;
            for (int j = 0; j < k; j++) {
                predicted += x.get(i, j) * betaHat.getValue(j);
            }
            double residual = y.getValue(i) - predicted;
            residualValues[i] = residual;
            rss += residual * residual;
        }

        double sigmaSquared = rss / (n - k);
        double sigma = Math.sqrt(sigmaSquared);

        CovarianceMatrix covMatrix = buildCovarianceMatrix(xTxInv, sigmaSquared, k);

        double meanY = DescriptiveStatistics.mean(y);
        double tss = 0.0;
        for (int i = 0; i < n; i++) {
            double dy = y.getValue(i) - meanY;
            tss += dy * dy;
        }
        double rSquared = (tss <= EPSILON) ? Double.NaN : 1.0 - (rss / tss);

        Vector residuals = new ArrayVector(residualValues);
        MultipleLinearRegressionResult result = new MultipleLinearRegressionResult(
                betaHat, sigma, covMatrix, residuals, rSquared, n, k
        );

        log.debug("fit completed: n={}, k={}, sigma={}, rSquared={}", n, k, sigma, rSquared);
        return result;
    }

    private CovarianceMatrix buildCovarianceMatrix(Matrix xTxInv, double sigmaSquared, int k) {
        double[][] covData = new double[k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                covData[i][j] = sigmaSquared * xTxInv.get(i, j);
            }
        }
        return new DenseCovarianceMatrix(covData);
    }

    private void validateInputs(Matrix x, Vector y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Design matrix x and response vector y must not be null.");
        }
        int n = x.rowCount();
        int k = x.columnCount();
        if (k < 1) {
            throw new IllegalArgumentException("Design matrix x must have at least 1 column.");
        }
        if (y.size() != n) {
            throw new IllegalArgumentException("Row count of x must match length of y.");
        }
        if (n <= k) {
            throw new IllegalArgumentException("Sample size n must be strictly greater than number of covariates k.");
        }
    }
}