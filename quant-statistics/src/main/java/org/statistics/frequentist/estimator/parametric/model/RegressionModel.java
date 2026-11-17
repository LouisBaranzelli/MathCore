package org.statistics.frequentist.estimator.parametric.model;

import org.math.vector.Vector;

/**
 * Modèle statistique conditionnel pour la régression (Y sachant X).
 */
public interface RegressionModel {

    /**
     * Vérifie si le vecteur de paramètres (ex: beta_0, beta_1, sigma) est valide.
     */
    boolean isValidParameterSet(Vector params);

    /**
     * Calcule la valeur prédite r(x) = E(Y | X = x).
     */
    double predict(double x, Vector params);

    /**
     * Calcule la log-vraisemblance d'une observation individuelle (y, x) sachant les paramètres.
     */
    double logLikelihood(double y, double x, Vector params);
}