package org.statistics.estimator.parametric.mle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ChiSquaredDistribution;

public class LikelihoodRatioTest {

    private static final Logger log = LoggerFactory.getLogger(LikelihoodRatioTest.class);



    /**
     * Calcule la statistique du LRT et la p-valeur associée.
     *
     * @param logLikelihoodFull       Log-vraisemblance du modèle non contraint (Theta)
     * @param logLikelihoodRestricted Log-vraisemblance du modèle contraint (Theta_0)
     * @param degreesOfFreedom        Nombre de restrictions (dim(Theta) - dim(Theta_0))
     */
    public static ChiSquareTestResult evaluate(double logLikelihoodFull, double logLikelihoodRestricted, int degreesOfFreedom) {
        if (degreesOfFreedom <= 0) {
            log.error("Tentative d'évaluation d'un LRT avec des degrés de liberté invalides : {}", degreesOfFreedom);
            throw new IllegalArgumentException("Le nombre de degrés de liberté doit être strictement positif.");
        }

        // Statistique lambda = 2 * ( log L(theta_hat) - log L(theta_0_hat) )
        double lambda = 2.0 * (logLikelihoodFull - logLikelihoodRestricted);

        // Protection contre les micro-erreurs d'arrondi flottant
        if (lambda < 0.0) {
            log.warn("Statistique lambda négative ({}), ajustée à 0.0 en raison d'erreurs d'arrondi.", lambda);
            lambda = 0.0;
        }

        // Calcul de la p-valeur via la fonction de survie : P(Chi^2_df > lambda) = 1 - CDF(lambda)
        ChiSquaredDistribution chiSquared = new ChiSquaredDistribution(degreesOfFreedom);
        double pValue = 1.0 - chiSquared.cdf(lambda);

        log.debug("LRT évalué avec succès : lambda = {}, df = {}, p-value = {}", lambda, degreesOfFreedom, pValue);

        return new ChiSquareTestResult(lambda, degreesOfFreedom, pValue);
    }
}