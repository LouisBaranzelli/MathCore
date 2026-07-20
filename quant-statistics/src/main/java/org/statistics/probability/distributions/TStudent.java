package org.statistics.probability.distributions;

import lombok.Getter;

public class TStudent implements ContinuousDistribution {

    private final double df;       // Degrés de liberté (\nu)
    @Getter
    private final double location; // Position / Moyenne (\mu)
    @Getter
    private final double scale;    // Échelle / Écart-type (\sigma)

    /**
     * Constructeur standard : t-Student centrée réduite (\mu = 0.0, \sigma = 1.0)
     *
     * @param df Degrés de liberté (doivent être strictement positifs)
     */
    public TStudent(double df) {
        this(df, 0.0, 1.0);
    }

    /**
     * Constructeur généralisé à 3 paramètres (Location-Scale Student's t)
     *
     * @param df       Degrés de liberté (\nu > 0)
     * @param location Paramètre de position / moyenne (\mu)
     * @param scale    Paramètre d'échelle (\sigma > 0)
     */
    public TStudent(double df, double location, double scale) {
        if (df <= 0.0) {
            throw new IllegalArgumentException("Degrees of freedom must be strictly positive, got " + df);
        }
        if (scale <= 0.0) {
            throw new IllegalArgumentException("Scale must be strictly positive, got " + scale);
        }
        this.df = df;
        this.location = location;
        this.scale = scale;
    }

    public double getDegreesOfFreedom() {
        return df;
    }

    @Override
    public double density(double x) {
        double z = (x - location) / scale;
        return densityStandard(z) / scale;
    }

    @Override
    public double cdf(double x) {
        double z = (x - location) / scale;
        return cdfStandard(z);
    }

    @Override
    public double inverseCdf(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("Probability must be in [0, 1], got " + p);
        }
        if (p == 0.0) return Double.NEGATIVE_INFINITY;
        if (p == 1.0) return Double.POSITIVE_INFINITY;

        return location + scale * inverseCdfStandard(p);
    }

    // =========================================================================
    // Formules Mathématiques pour la Student Standard t(df)
    // =========================================================================

    private double densityStandard(double z) {
        // Log-densité pour éviter les overflow/underflow numériques
        double logNom = logGamma((df + 1.0) / 2.0);
        double logDenom = 0.5 * Math.log(df * Math.PI) + logGamma(df / 2.0);
        double logFactor = -((df + 1.0) / 2.0) * Math.log(1.0 + (z * z) / df);

        return Math.exp(logNom - logDenom + logFactor);
    }

    private double cdfStandard(double z) {
        if (z == 0.0) {
            return 0.5;
        }

        // Relation avec la fonction Bêta incomplète régularisée
        double xt = df / (df + z * z);
        double beta = 0.5 * regularizedIncompleteBeta(xt, df / 2.0, 0.5);

        return z > 0.0 ? 1.0 - beta : beta;
    }

    private double inverseCdfStandard(double p) {
        if (p == 0.5) return 0.0;

        // Inversion numérique par recherche dichotomique sécurisée + Newton-Raphson
        double low = -100.0;
        double high = 100.0;

        // Cadrage des bornes dynamiques si p est très proche de 0 ou 1
        while (cdfStandard(low) > p) low *= 2.0;
        while (cdfStandard(high) < p) high *= 2.0;

        double guess = 0.0;
        for (int i = 0; i < 100; i++) {
            double cdfVal = cdfStandard(guess);
            double pdfVal = densityStandard(guess);
            double err = cdfVal - p;

            if (Math.abs(err) < 1e-12) {
                return guess;
            }

            if (cdfVal < p) low = guess;
            else high = guess;

            if (pdfVal > 1e-15) {
                double nextGuess = guess - err / pdfVal;
                if (nextGuess > low && nextGuess < high) {
                    guess = nextGuess;
                    continue;
                }
            }
            guess = 0.5 * (low + high);
        }
        return guess;
    }

    // =========================================================================
    // Outils Numériques : LogGamma & Beta Incomplète Régularisée
    // =========================================================================

    private static double logGamma(double x) {
        // Approximation de Lanczos (g=5, n=7)
        double[] p = {
                1.000000000190015,
                76.18009172947146,
                -86.50532032941677,
                24.01409824083091,
                -1.231739572450155,
                0.1208650973866179e-2,
                -0.5395239384953e-5
        };
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = p[0];
        for (int j = 1; j <= 6; j++) {
            y += 1.0;
            ser += p[j] / y;
        }
        return -tmp + Math.log(Math.sqrt(2 * Math.PI) * ser / x);
    }

    private static double regularizedIncompleteBeta(double x, double a, double b) {
        if (x <= 0.0) return 0.0;
        if (x >= 1.0) return 1.0;

        // Relation de symétrie si nécessaire pour garantir la convergence de la fraction continue
        if (x > (a + 1.0) / (a + b + 2.0)) {
            return 1.0 - regularizedIncompleteBeta(1.0 - x, b, a);
        }

        double front = Math.exp(a * Math.log(x) + b * Math.log(1.0 - x)
                - logGamma(a) - logGamma(b) + logGamma(a + b)) / a;

        // Fraction continue de Lentz
        double f = 1.0, c = 1.0, d = 0.0;
        for (int i = 0; i <= 200; i++) {
            int m = i / 2;
            double numerator;
            if (i == 0) {
                numerator = 1.0;
            } else if (i % 2 == 0) {
                numerator = (m * (b - m) * x) / ((a + 2 * m - 1) * (a + 2 * m));
            } else {
                numerator = -((a + m) * (a + b + m) * x) / ((a + 2 * m) * (a + 2 * m + 1));
            }

            d = 1.0 + numerator * d;
            if (Math.abs(d) < 1e-30) d = 1e-30;
            c = 1.0 + numerator / c;
            if (Math.abs(c) < 1e-30) c = 1e-30;
            d = 1.0 / d;
            double delta = c * d;
            f *= delta;
            if (Math.abs(delta - 1.0) < 1e-12) break;
        }
        return front * (f - 1.0);
    }
}