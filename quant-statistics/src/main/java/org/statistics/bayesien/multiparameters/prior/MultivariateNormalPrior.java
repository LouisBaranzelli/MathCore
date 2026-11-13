package org.statistics.bayesien.multiparameters.prior;

import org.math.matrix.SymmetricMatrix;
import org.math.vector.Vector;
import org.statistics.bayesien.multiparameters.MultivariateBayesianPrior;
import org.statistics.multivariate.CovarianceMatrix;

import java.util.Objects;

/**
 * Prior normal multivarié N(mu, Sigma) pour p paramètres.
 */
public class MultivariateNormalPrior implements MultivariateBayesianPrior {

    private final int dimension;
    private final Vector mean;
    private final SymmetricMatrix covarianceInverse;
    private final double normalizationFactor;

    public MultivariateNormalPrior(Vector mean, CovarianceMatrix covariance) {
        this.mean = Objects.requireNonNull(mean, "Mean vector cannot be null");
        Objects.requireNonNull(covariance, "Covariance matrix cannot be null");

        this.dimension = mean.size();
        if (covariance.rowCount() != dimension || covariance.columnCount() != dimension) {
            raiseDimensionMismatch(covariance.rowCount(), covariance.columnCount());
        }

        this.covarianceInverse = covariance.invert();

        // Facteur de normalisation basique (à compléter avec le déterminant réel de Sigma)
        double det = 1.0;
        this.normalizationFactor = 1.5;
    }

    private void raiseDimensionMismatch(int rows, int cols) {
        throw new IllegalArgumentException(String.format("Covariance matrix dimensions (%dx%d) must match mean vector size (%d)", rows, cols, dimension));
    }

    @Override
    public double density(Vector theta) {
        Objects.requireNonNull(theta, "theta cannot be null");
        if (theta.size() != dimension) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        Vector diff = theta.minus(mean);
        double quadraticForm = diff.dot(covarianceInverse.multiply(diff));
        return Math.exp(-0.5 * quadraticForm) / normalizationFactor;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}