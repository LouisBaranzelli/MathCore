package org.math.probability.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.math.function.special.Gamma; // Ressource externe statique

/**
 * Représente une loi du Chi-deux (Chi-Squared Distribution) à k degrés de liberté.
 * Appartient au module fondamental de probabilité.
 */
public class ChiSquaredDistribution {

    private static final Logger log = LoggerFactory.getLogger(ChiSquaredDistribution.class);

    private final int degreesOfFreedom;

    public ChiSquaredDistribution(int degreesOfFreedom) {
        if (degreesOfFreedom <= 0) {
            log.error("Attempted to instantiate ChiSquaredDistribution with invalid degrees of freedom: {}", degreesOfFreedom);
            throw new IllegalArgumentException("Le nombre de degrés de liberté doit être strictement positif.");
        }
        this.degreesOfFreedom = degreesOfFreedom;
        log.debug("Created ChiSquaredDistribution with k={}", degreesOfFreedom);
    }

    public double density(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        double k = degreesOfFreedom;
        double halfK = k / 2.0;

        // Utilisation du calcul en log-espace pour éviter l'underflow/overflow numérique
        double logPdf = (halfK - 1.0) * Math.log(x) - (x / 2.0) - halfK * Math.log(2.0) - Math.log(Gamma.gamma(halfK));
        return Math.exp(logPdf);
    }

    public double cdf(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        return Gamma.regularizedGammaP(degreesOfFreedom / 2.0, x / 2.0);
    }

    public double inverseCdf(double p) {
        if (p <= 0.0 || p >= 1.0) {
            log.warn("Called inverseCdf with probability out of bounds: {}", p);
            throw new IllegalArgumentException("La probabilité p doit être strictement comprise dans l'intervalle ]0, 1[.");
        }

        double x = degreesOfFreedom; // Espérance comme point de départ
        double tolerance = 1e-12;
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            double cdfValue = cdf(x);
            double diff = cdfValue - p;

            if (Math.abs(diff) < tolerance) {
                return x;
            }

            double pdfValue = density(x);
            if (pdfValue == 0.0) {
                log.warn("Zero derivative encountered while inverting CDF for p = {}.", p);
                break;
            }

            x = x - diff / pdfValue;
            if (x <= 0.0) {
                x = tolerance;
            }
        }

        log.error("Newton-Raphson failed to converge for inverseCdf with p = {}", p);
        throw new IllegalStateException("L'algorithme de Newton-Raphson n'a pas convergé pour inverseCdf.");
    }

    public int getDegreesOfFreedom() {
        return degreesOfFreedom;
    }
}