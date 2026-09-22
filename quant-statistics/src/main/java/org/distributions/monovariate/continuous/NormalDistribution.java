package org.distributions.monovariate.continuous;


import lombok.Getter;
import org.math.function.special.Erf;
import org.math.function.special.NormalInverseCdf;
@Getter
public class NormalDistribution implements ContinuousDistribution {

    private final double mu;
    private final double sigma;
    private static final double SQRT_TWO_PI = Math.sqrt(2 * Math.PI);


    public NormalDistribution(double mu, double std) {
        if (std <= 0) {
            throw new IllegalArgumentException("L'écart-type doit être strictement positif.");
        }
        this.mu = mu;
        this.sigma = std;
    }

    @Override
    public double cdf(double x) {
        double z = (x - mu) / sigma;
        return 0.5 * (1.0 + Erf.erf(z / Math.sqrt(2)));
    }

    @Override
    public double inverseCdf(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("La probabilité doit être dans ]0, 1[.");
        }
        return mu + sigma * NormalInverseCdf.compute(p);
    }

    @Override
    public double density(double x) {
        double z = (x - mu) / sigma;
        return Math.exp(-0.5 * z * z) / (sigma * SQRT_TWO_PI);
    }
}
