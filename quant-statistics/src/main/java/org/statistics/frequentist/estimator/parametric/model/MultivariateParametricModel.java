package org.statistics.frequentist.estimator.parametric.model;

import org.math.vector.Vector;
import org.statistics.multivariate.CovarianceMatrix;
// Assurez-vous d'importer votre classe de matrice de covariance selon votre package
// import org.math.matrix.CovarianceMatrix;

/**
 * Interface représentant un modèle paramétrique multivarié.
 * Adaptée pour des observations vectorielles (ex: portefeuilles, modèles multi-actifs, CAPM).
 *
 * @param <T> Le type de la distribution multivariée associée
 */
public interface MultivariateParametricModel<T> {

    /**
     * Crée la distribution multivariée sous-jacente à partir du vecteur de paramètres.
     */
    T createDistribution(Vector params);

    /**
     * Valide si le jeu de paramètres est dans le domaine de définition (ex: matrices définies positives).
     */
    boolean isValidParameterSet(Vector params);

    /**
     * Calcule la log-vraisemblance pour une observation vectorielle unique x.
     *
     * @param x l'observation vectorielle (ex: rendement des 100 actifs à l'instant t)
     * @param params le vecteur des paramètres theta du modèle
     * @return la log-vraisemblance de cette observation
     */
    double logLikelihood(Vector x, Vector params);

    /**
     * Retourne le vecteur des moyennes (espérances) prédites par le modèle pour theta.
     */
    Vector mean(Vector theta);

    /**
     * Retourne la matrice de covariance (ou de variance-covariance des résidus) pour theta.
     */
    CovarianceMatrix covariance(Vector theta);
}