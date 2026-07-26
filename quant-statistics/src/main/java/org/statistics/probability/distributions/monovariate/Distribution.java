package org.statistics.probability.distributions.monovariate;

public interface Distribution {
    double cdf(double x);

    double inverseCdf(double x);
}
