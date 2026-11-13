package org.statistics.bayesien.multiparameters.prior;

import org.math.vector.Vector;
import org.statistics.bayesien.multiparameters.MultivariateBayesianPrior;

import java.util.Objects;

/**
 * Représente un prior uniforme multidimensionnel sur un hypercube délimité par [thetaMin, thetaMax].
 *
 * <p><b>Quelle information apporte-t-il vraiment ?</b><br>
 * Contrairement à l'idée reçue d'un « a priori plat » ou d'une absence totale d'information,
 * ce prior impose deux contraintes mathématiques et conceptuelles très fortes :</p>
 *
 * <ol>
 *   <li><b>Le support (Les bornes physiques ou économiques) :</b> Il définit une barrière infranchissable :
 *       la vraie valeur du paramètre est obligatoirement comprise entre ces limites. Tout ce qui
 *       se trouve en dehors a une densité de probabilité rigoureusement nulle (0). Cela permet
 *       d'exclure d'emblée les valeurs aberrantes (par exemple, des taux ou des volatilités négatives)
 *       et d'éviter que les algorithmes d'optimisation ou de simulation ne dérivent vers des zones absurdes.</li>
 *   <li><b>L'équirépartition :</b> À l'intérieur de cet intervalle, toutes les valeurs ont exactement
 *       la même chance a priori, sans qu'aucune ne soit favorisée par rapport à une autre.</li>
 * </ol>
 */
public class MultivariateUniformPrior implements MultivariateBayesianPrior {

    private final Vector thetaMin;
    private final Vector thetaMax;
    private final int dimension;
    private final double densityValue;

    public MultivariateUniformPrior(Vector thetaMin, Vector thetaMax) {
        this.thetaMin = Objects.requireNonNull(thetaMin, "thetaMin cannot be null");
        this.thetaMax = Objects.requireNonNull(thetaMax, "thetaMax cannot be null");

        if (thetaMin.size() != thetaMax.size()) {
            throw new IllegalArgumentException("thetaMin and thetaMax must have the same dimension");
        }

        this.dimension = thetaMin.size();
        double volume = 1.0;

        for (int i = 0; i < dimension; i++) {
            double min = thetaMin.getValue(i);
            double max = thetaMax.getValue(i);
            if (min >= max) {
                throw new IllegalArgumentException(String.format(
                        "Invalid bounds at index %d: min (%f) must be less than max (%f)", i, min, max));
            }
            volume *= (max - min);
        }

        this.densityValue = 1.0 / volume;
    }

    @Override
    public double density(Vector theta) {
        Objects.requireNonNull(theta, "theta cannot be null");
        if (theta.size() != dimension) {
            throw new IllegalArgumentException(String.format(
                    "Dimension mismatch: expected %d, got %d", dimension, theta.size()));
        }

        for (int i = 0; i < dimension; i++) {
            double val = theta.getValue(i);
            if (val < thetaMin.getValue(i) || val > thetaMax.getValue(i)) {
                return 0.0;
            }
        }
        return densityValue;
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}