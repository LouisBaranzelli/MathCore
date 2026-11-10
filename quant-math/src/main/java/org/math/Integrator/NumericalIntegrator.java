package org.math.Integrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Interface représentant un algorithme d'intégration numérique univariée.
 * Permet d'approcher l'intégrale d'une fonction sur un intervalle [a, b].
 */
public interface NumericalIntegrator {

    /**
     * Calcule l'intégrale numérique de la fonction f sur l'intervalle [a, b].
     *
     * @oaram f la fonction à intégrer x -> f(x)
     * @param a la borne inférieure de l'intervalle
     * @param b la borne supérieure de l'intervalle
     * @return la valeur approchée de l'intégrale
     */
    double integrate(ToDoubleFunction<Double> f, double a, double b);

 
    default void validateIntegrationBounds(double a, double b) {
        if (Double.isNaN(a) || Double.isNaN(b) || Double.isInfinite(a) || Double.isInfinite(b)) {
            throw new IllegalArgumentException("Integration bounds must be finite numbers: a=" + a + ", b=" + b);
        }
        if (a > b) {
            throw new IllegalArgumentException("Lower bound 'a' cannot be greater than upper bound 'b': a=" + a + ", b=" + b);
        }
    }
}