package org.statistics.frequentist.estimator.parametric.model.logistic;

import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.multivariate.CovarianceMatrix;
import org.statistics.multivariate.DenseCovarianceMatrix;

/**
 * Estimateur par maximum de vraisemblance pour la régression logistique multiple
 * via l'algorithme Iteratively Reweighted Least Squares (IRLS).
 */
public class MultipleLogisticRegressionEstimator {

    private static final Logger log = LoggerFactory.getLogger(MultipleLogisticRegressionEstimator.class);

    private static final int DEFAULT_MAX_ITERATIONS = 100;
    private static final double DEFAULT_TOLERANCE = 1e-8;
    private static final double PROBABILITY_EPSILON = 1e-12;

    private final int maxIterations;
    private final double tolerance;

    public MultipleLogisticRegressionEstimator() {
        this(DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    public MultipleLogisticRegressionEstimator(int maxIterations, double tolerance) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be at least 1.");
        }
        if (tolerance <= 0.0 || Double.isNaN(tolerance) || Double.isInfinite(tolerance)) {
            throw new IllegalArgumentException("tolerance must be a positive finite number.");
        }
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * Estime le vecteur des paramètres {@code beta} de dimension {@code k}.
     *
     * @param x matrice de design {@code n x k} (inclure une colonne de {@code 1.0} pour l'intercept)
     * @param y vecteur binaire des observations de taille {@code n} ({@code 0.0} ou {@code 1.0})
     * @return vecteur estimé des coefficients de taille {@code k}
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
     * Ajuste le modèle logistique multiple par IRLS et retourne les statistiques d'inférence.
     *
     * @param x matrice de design {@code n x k} (inclure une colonne de {@code 1.0} pour l'intercept)
     * @param y vecteur binaire des observations de taille {@code n} ({@code 0.0} ou {@code 1.0})
     * @return résultat contenant les coefficients, la log-vraisemblance, la covariance
     *         asymptotique, les probabilités ajustées et l'état de convergence
     */
    public MultipleLogisticRegressionResult fit(Matrix x, Vector y) {
        log.trace("fit called with xRows={}, xCols={}, ySize={}",
                x == null ? -1 : x.rowCount(),
                x == null ? -1 : x.columnCount(),
                y == null ? -1 : y.size());
        validateInputs(x, y);

        int n = x.rowCount();
        int k = x.columnCount();

        double[] beta = new double[k];
        double[] probabilities = new double[n];
        for (int i = 0; i < n; i++) {
            probabilities[i] = 0.5;
        }

        int iter = 0;
        boolean converged = false;
        Matrix lastInformationInverse = null;

        for (iter = 1; iter <= maxIterations; iter++) {
            double[] workingResponse = new double[n];
            double[] weights = new double[n];

            for (int i = 0; i < n; i++) {
                double eta = dotRow(x, i, beta, k);
                double p = clampProbability(sigmoid(eta));
                double w = p * (1.0 - p);
                double yi = y.getValue(i);

                probabilities[i] = p;
                weights[i] = w;
                workingResponse[i] = eta + (yi - p) / w;
            }

            // Calcul de X^T W X et X^T W z
            double[][] xTwxData = new double[k][k];
            double[] xTwzData = new double[k];

            for (int i = 0; i < n; i++) {
                double wi = weights[i];
                double zi = workingResponse[i];
                for (int j = 0; j < k; j++) {
                    double xij = x.get(i, j);
                    double wixij = wi * xij;
                    xTwzData[j] += wixij * zi;
                    for (int m = 0; m < k; m++) {
                        xTwxData[j][m] += wixij * x.get(i, m);
                    }
                }
            }

            Matrix xTwx = new DenseMatrix(xTwxData);
            Matrix xTwxInv = xTwx.invert();
            Vector xTwz = new ArrayVector(xTwzData);
            Vector nextBetaVec = xTwxInv.multiply(xTwz);

            double maxDelta = 0.0;
            for (int j = 0; j < k; j++) {
                double nextVal = nextBetaVec.getValue(j);
                double delta = Math.abs(nextVal - beta[j]);
                if (delta > maxDelta) {
                    maxDelta = delta;
                }
                beta[j] = nextVal;
            }

            lastInformationInverse = xTwxInv;

            if (maxDelta < tolerance) {
                converged = true;
                break;
            }
        }

        int executedIterations = converged ? iter : maxIterations;
        double logLikelihood = 0.0;
        for (int i = 0; i < n; i++) {
            double eta = dotRow(x, i, beta, k);
            double p = clampProbability(sigmoid(eta));
            probabilities[i] = p;
            double yi = y.getValue(i);
            logLikelihood += yi * eta - log1pExp(eta);
        }

        if (!converged) {
            log.warn("IRLS did not converge within {} iterations.", maxIterations);
        }

        CovarianceMatrix covMatrix = toCovarianceMatrix(lastInformationInverse, k);
        MultipleLogisticRegressionResult result = new MultipleLogisticRegressionResult(
                new ArrayVector(beta),
                logLikelihood,
                covMatrix,
                new ArrayVector(probabilities),
                executedIterations,
                converged
        );

        log.debug("fit completed: converged={}, iterations={}, logLikelihood={}",
                converged, executedIterations, logLikelihood);
        return result;
    }

    private double dotRow(Matrix x, int row, double[] beta, int k) {
        double sum = 0.0;
        for (int j = 0; j < k; j++) {
            sum += x.get(row, j) * beta[j];
        }
        return sum;
    }

    private double sigmoid(double eta) {
        if (eta >= 0.0) {
            return 1.0 / (1.0 + Math.exp(-eta));
        }
        double expEta = Math.exp(eta);
        return expEta / (1.0 + expEta);
    }

    private double log1pExp(double eta) {
        if (eta > 0.0) {
            return eta + Math.log1p(Math.exp(-eta));
        }
        return Math.log1p(Math.exp(eta));
    }

    private double clampProbability(double p) {
        if (p < PROBABILITY_EPSILON) {
            return PROBABILITY_EPSILON;
        }
        if (p > 1.0 - PROBABILITY_EPSILON) {
            return 1.0 - PROBABILITY_EPSILON;
        }
        return p;
    }

    /**
     * Convertit l'inverse de l'information de Fisher en matrice de covariance symétrique.
     */
    private CovarianceMatrix toCovarianceMatrix(Matrix xTwxInv, int k) {
        log.trace("toCovarianceMatrix called with k={}", k);
        if (xTwxInv == null) {
            throw new IllegalStateException("Information inverse matrix was not computed.");
        }
        double[][] data = new double[k][k];
        for (int i = 0; i < k; i++) {
            data[i][i] = xTwxInv.get(i, i);
            for (int j = i + 1; j < k; j++) {
                double sym = 0.5 * (xTwxInv.get(i, j) + xTwxInv.get(j, i));
                data[i][j] = sym;
                data[j][i] = sym;
            }
        }
        CovarianceMatrix cov = new DenseCovarianceMatrix(data);
        log.debug("toCovarianceMatrix completed with dimension={}", k);
        return cov;
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
        for (int i = 0; i < n; i++) {
            double yi = y.getValue(i);
            if (yi != 0.0 && yi != 1.0) {
                throw new IllegalArgumentException("Response vector y must contain only binary values 0.0 or 1.0.");
            }
            for (int j = 0; j < k; j++) {
                double xij = x.get(i, j);
                if (Double.isNaN(xij) || Double.isInfinite(xij)) {
                    throw new IllegalArgumentException("Design matrix x must contain only finite values.");
                }
            }
        }
    }
}