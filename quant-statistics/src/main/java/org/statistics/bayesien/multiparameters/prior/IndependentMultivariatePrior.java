package org.statistics.bayesien.multiparameters.prior;

import org.math.vector.Vector;
import org.statistics.bayesien.monoparameter.MonovariateBayesianPrior;
import org.statistics.bayesien.multiparameters.MultivariateBayesianPrior;

import java.util.Objects;

/**
 * Représente un prior multidimensionnel formé par le produit de priors univariés indépendants.
 * Par exemple : theta_1 (moyenne) ~ Normal, theta_2 (variance) ~ Gamma.
 */
public class IndependentMultivariatePrior implements MultivariateBayesianPrior {

    private final MonovariateBayesianPrior[] univariatePriors;
    private final int dimension;

    public IndependentMultivariatePrior(MonovariateBayesianPrior... univariatePriors) {
        Objects.requireNonNull(univariatePriors, "Priors array cannot be null");
        if (univariatePriors.length == 0) {
            throw new IllegalArgumentException("At least one univariate prior must be provided");
        }
        this.univariatePriors = univariatePriors.clone();
        this.dimension = univariatePriors.length;

        for (int i = 0; i < dimension; i++) {
            Objects.requireNonNull(univariatePriors[i], "Univariate prior cannot be null at index " + i);
        }
    }

    @Override
    public double density(Vector theta) {
        Objects.requireNonNull(theta, "theta cannot be null");
        if (theta.size() != dimension) {
            String msg = String.format("Dimension mismatch: expected %d priors, but theta has size %d", dimension, theta.size());
            throw new IllegalArgumentException(msg);
        }

        double jointDensity = 1.0;
        for (int i = 0; i < dimension; i++) {
            double paramValue = theta.getValue(i);
            double priorDensity = univariatePriors[i].density(paramValue);

            if (priorDensity == 0.0) {
                return 0.0;
            }
            jointDensity *= priorDensity;
        }

        return jointDensity;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}