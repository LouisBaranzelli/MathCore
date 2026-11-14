package org.math.Integrator.multivariate;

import org.math.vector.Vector;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Interface représentant un algorithme d'intégration numérique multivariée.
 * Permet d'approcher l'intégrale multiple d'une fonction sur un domaine délimité par des vecteurs de bornes [a, b].
 */
public interface MultivariateNumericalIntegrator {

    /**
     * Calcule l'intégrale numérique multiple de la fonction f sur le domaine défini par les vecteurs a et b.
     *
     * @param f la fonction à intégrer theta -> f(theta)
     * @param a le vecteur des bornes inférieures pour chaque dimension
     * @param b le vecteur des bornes supérieures pour chaque dimension
     * @return la valeur approchée de l'intégrale multiple
     */
    double integrate(ToDoubleFunction<Vector> f, Vector a, Vector b);

    /**
     * Valide que les vecteurs de bornes sont cohérents, de même dimension et finis.
     *
     * @param a vecteur des bornes inférieures
     * @param b vecteur des bornes supérieures
     */
    default void validateIntegrationBounds(Vector a, Vector b) {
        Objects.requireNonNull(a, "Lower bounds vector 'a' cannot be null");
        Objects.requireNonNull(b, "Upper bounds vector 'b' cannot be null");

        if (a.size() != b.size()) {
            throw new IllegalArgumentException(
                    String.format("Dimension mismatch between bounds: a.size()=%d, b.size()=%d", a.size(), b.size())
            );
        }

        int dim = a.size();
        for (int i = 0; i < dim; i++) {
            double lower = a.getValue(i);
            double upper = b.getValue(i);

            if (Double.isNaN(lower) || Double.isNaN(upper) || Double.isInfinite(lower) || Double.isInfinite(upper)) {
                throw new IllegalArgumentException(
                        String.format("Integration bounds must be finite numbers at dimension %d: a[%d]=%f, b[%d]=%f", i, i, lower, i, upper)
                );
            }
            if (lower > upper) {
                throw new IllegalArgumentException(
                        String.format("Lower bound cannot be greater than upper bound at dimension %d: a[%d]=%f > b[%d]=%f", i, i, lower, i, upper)
                );
            }
        }
    }
}