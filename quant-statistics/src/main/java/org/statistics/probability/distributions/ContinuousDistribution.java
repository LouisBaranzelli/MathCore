package org.statistics.probability.distributions;

import java.util.random.RandomGenerator;

public interface ContinuousDistribution extends Distribution {

    default double survivalFunction(double x) {
        return 1.0 - cdf(x);
    }

    double density(double x);

    default double getSample(RandomGenerator random){
        double probability = random.nextDouble();
        return inverseCdf(probability);
    }
}
