package org.statistics.probability.distributions.monovariate;

import java.util.random.RandomGenerator;

public interface ContinuousDistribution extends Distribution {

    default double survivalFunction(double x) {
        return 1.0 - cdf(x);
    }

    /**
     * Calcule la densite de probabilite f(x).
     */
    double density(double x);

    /**
     * Calcule la fonction quantile F^-1(p).
     */
    double inverseCdf(double p);

    default double getSample(RandomGenerator random){
        double probability = random.nextDouble();
        return inverseCdf(probability);
    }
}
