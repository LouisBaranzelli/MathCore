package org.statistics.probability.distributions;

public interface Distribution {
    double cdf(double x);

    double inverseCdf(double x);
}
