package org.statistics.frequentist.estimator.parametric.model;

import org.math.matrix.Matrix;
import org.math.vector.Vector;

/**
 * Contrat commun pour l'estimation de modèles de régression à covariables matricielles.
 *
 * @param <R> type du résultat détaillé produit par l'ajustement
 */
public interface MatrixRegressionEstimator<R> {

    /**
     * Estime uniquement le vecteur des coefficients de taille k.
     */
    Vector estimate(Matrix x, Vector y);

    /**
     * Ajuste le modèle et retourne le résultat complet d'inférence associé.
     */
    R fit(Matrix x, Vector y);
}