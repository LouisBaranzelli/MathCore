package org.statistics.bayesien.multiparameters;

import org.math.vector.Vector;

/**
 * Représente une loi a priori multidimensionnelle (Prior) p(theta)
 * pour un modèle à p paramètres.
 */
public interface MultivariateBayesianPrior {

    /**
     * Calcule la densité de probabilité a priori pour un vecteur de paramètres donné.
     *
     * @param theta Le vecteur des paramètres theta de dimension p
     * @return La densité de probabilité p(theta) (>= 0.0)
     */
    double density(Vector theta);


    int getDimension();
}