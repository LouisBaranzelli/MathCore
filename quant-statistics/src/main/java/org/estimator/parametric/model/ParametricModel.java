package org.estimator.parametric.model;

import org.math.vector.Vector;

/**
 * Modèle probabiliste capable de mapper un Vector vers une instance de distribution.
 *
 * @param <T> Le type de distribution produit
 */
public interface ParametricModel<T> {

    T createDistribution(Vector params);

    boolean isValidParameterSet(Vector params);

    double logLikelihood(double x, Vector params);
}