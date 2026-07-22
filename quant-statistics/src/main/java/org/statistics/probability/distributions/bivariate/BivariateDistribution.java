package org.statistics.probability.distributions.bivariate;

/**
 * Interface représentant une distribution de probabilité bivariée (2D).
 */
public interface BivariateDistribution {

    double cdf(double x, double y);

    double inverseConditionalCdfY(double p, double xGiven);

    double inverseConditionalCdfX(double p, double yGiven);
}