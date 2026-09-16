package org.statistics.probability.distributions.monovariate;


import org.statistics.probability.tools.Erf;
import org.statistics.probability.tools.NormalInverseCdf;

/**
 * Modelise la loi Log-Normale de parametres mu et sigma > 0.
 */
public class LogNormalDistribution implements ContinuousDistribution {

    private static final double SQRT_TWO_PI = Math.sqrt(2 * Math.PI);

    private final double mu;
    private final double sigma;

    public LogNormalDistribution(double mu, double sigma) {
        if (sigma <= 0.0) {
            throw new IllegalArgumentException("L'ecart-type sigma doit etre strictement positif.");
        }
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public double cdf(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        double z = (Math.log(x) - mu) / sigma;
        return 0.5 * (1.0 + Erf.erf(z / Math.sqrt(2)));
    }

    @Override
    public double inverseCdf(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("La probabilite doit etre dans ]0, 1[.");
        }
        return Math.exp(mu + sigma * NormalInverseCdf.compute(p));
    }

    @Override
    public double density(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        double logX = Math.log(x);
        double z = (logX - mu) / sigma;
        return Math.exp(-0.5 * z * z) / (x * sigma * SQRT_TWO_PI);
    }

    public double getMu() {
        return mu;
    }

    public double getSigma() {
        return sigma;
    }
}