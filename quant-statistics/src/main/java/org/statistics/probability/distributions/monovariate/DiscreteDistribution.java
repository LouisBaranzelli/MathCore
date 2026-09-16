package org.statistics.probability.distributions.monovariate;

public interface DiscreteDistribution extends Distribution {

    /**
     * Calcule la masse de probabilite P(X = k).
     */
    double pmf(int k);
}