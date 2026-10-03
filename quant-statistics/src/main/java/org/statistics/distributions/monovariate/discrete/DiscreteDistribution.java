package org.statistics.distributions.monovariate.discrete;

import org.statistics.distributions.monovariate.Distribution;

public interface DiscreteDistribution extends Distribution {

    /**
     * Calcule la masse de probabilite P(X = k).
     */
    double pmf(int k);
}