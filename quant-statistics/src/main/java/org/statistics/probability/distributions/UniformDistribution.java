package org.statistics.probability.distributions;

public class UniformDistribution implements ContinuousDistribution {
    private final double a;
    private final double b;

    public UniformDistribution(double a, double b) {
        if (a >= b) {
            throw new IllegalArgumentException("La borne 'a' doit être inférieure à 'b'.");
        }
        this.a = a;
        this.b = b;
    }

    @Override
    public double cdf(double x) {
        if (x < a) return 0.0;
        if (x > b) return 1.0;
        return (x - a) / (b - a);
    }

    @Override
    public double inverseCdf(double p) {
       if (p > 1 || p <= 0){
           throw new IllegalArgumentException("propability must be between ]0 : 1], got " + p);
       }
        return a + p * (b - a);
    }

    @Override
    public double density(double x) {
        if (x < a || x > b) return 0.0;
        return 1.0 / (b - a);
    }


}