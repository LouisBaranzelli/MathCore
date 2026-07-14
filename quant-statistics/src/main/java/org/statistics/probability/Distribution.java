package org.statistics.probability;

import java.util.Random;
import java.util.random.RandomGenerator;

public interface Distribution {

    double cdf(double x);

    double inverseCdf(double x);

    double density(double x);

    default double getSample(RandomGenerator random){
        double seed = random.nextDouble();
        return inverseCdf(seed);
    }
}
