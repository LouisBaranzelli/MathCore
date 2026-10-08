package org.statistics.estimator.parametric.uncertainty;

import org.math.function.MultivariateFunction;
import org.statistics.estimator.nonparametric.bootstrap.VectorPointEstimate;

import java.util.function.Function;

@FunctionalInterface
public interface UncertaintyEstimator {
    /**
     * Calcule l'erreur type (SE) de la fonction g(theta)
     *
     * @param result Le résultat d'estimation contenant theta_hat et la covariance -H^-1
     * @param gFunction La transformation g: DoubleVector -> Double
     * @return L'erreur type estimée (se)
     *
     * Objectif : calculer la SE d'une fonction qui utilise les résultats du VectorPointEstimate.
     * Exemple : si le Sharpe Ratio a besoin de ces paramètres estimés, quelle est la SE de mon Sharpe Ratio ?
     * Permet alors de calculer des intervalles de confiance sur cet indicateur.
     */
    double estimateStandardError(VectorPointEstimate result, MultivariateFunction gFunction);
}