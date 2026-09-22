package org.distributions.monovariate.discrete;


import java.util.logging.Logger;

/**
 * Modélise la loi de Poisson de paramètre lambda > 0.
 */
public class PoissonDistribution implements DiscreteDistribution {

    private static final Logger LOGGER = Logger.getLogger(PoissonDistribution.class.getName());
    private final double lambda;

    public PoissonDistribution(double lambda) {
        if (lambda <= 0.0) {
            LOGGER.severe("Invalid lambda parameter: " + lambda + ". Lambda must be strictly positive.");
            throw new IllegalArgumentException("Le paramètre lambda doit être strictement positif.");
        }
        this.lambda = lambda;
    }

    @Override
    public double pmf(int k) {
        if (k < 0) {
            return 0.0;
        }
        // Calcul via log-gamma pour éviter le dépassement de capacité (overflow) des factorielles
        return Math.exp(k * Math.log(lambda) - lambda - logFactorial(k));
    }

    @Override
    public double cdf(double x) {
        if (x < 0) {
            return 0.0;
        }
        int kMax = (int) Math.floor(x);
        double sum = 0.0;
        for (int k = 0; k <= kMax; k++) {
            sum += pmf(k);
        }
        return Math.min(1.0, sum);
    }

    public double getLambda() {
        return lambda;
    }

    private static double logFactorial(int k) {
        double logFact = 0.0;
        for (int i = 2; i <= k; i++) {
            logFact += Math.log(i);
        }
        return logFact;
    }
}