package org.distributions.monovariate.discrete;

import org.distributions.monovariate.Distribution;

public interface DiscreteDistribution extends Distribution {

    /**
     * Calcule la masse de probabilite P(X = k).
     */
    double pmf(int k);
}