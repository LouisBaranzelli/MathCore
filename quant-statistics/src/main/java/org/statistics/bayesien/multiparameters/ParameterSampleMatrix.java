package org.statistics.bayesien.multiparameters;

import org.math.matrix.DenseMatrix;
import org.math.vector.Vector;
import org.statistics.data.Sample;

import java.util.Objects;

/**
 * Représente l'historique complet des tirages MCMC sous forme d'une matrice dense (B x p).
 *
 * <p><b>Mémoire & Performance :</b> L'utilisation d'une structure plate contiguë
 * (via {@link DenseMatrix}) évite l'instanciation de millions d'objets Java (comme des tableaux
 * de {@link Vector}), réduisant drastiquement la pression sur le Garbage Collector (GC)
 * et maximisant la localité des données en cache CPU lors des extractions marginales.</p>
 */
public class ParameterSampleMatrix {

    private final DenseMatrix matrix;
    private final int numSamples; // B (lignes)
    private final int numParams;  // p (colonnes)

    public ParameterSampleMatrix(double[][] data) {
        Objects.requireNonNull(data, "Data matrix cannot be null");
        this.matrix = new DenseMatrix(data);
        this.numSamples = matrix.rowCount();
        this.numParams = matrix.columnCount();
    }

    public ParameterSampleMatrix(DenseMatrix matrix) {
        this.matrix = Objects.requireNonNull(matrix, "Matrix cannot be null");
        this.numSamples = matrix.rowCount();
        this.numParams = matrix.columnCount();
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getNumParams() {
        return numParams;
    }

    /**
     * Récupère un vecteur complet pour une itération donnée (la ligne b).
     */
    public Vector getSampleAt(int iteration) {
        return matrix.getRow(iteration);
    }

    /**
     * Extrait la distribution marginale d'un paramètre spécifique (la colonne k)
     * pour alimenter directement un objet {@link Sample}.
     */
    public Sample extractMarginal(int parameterIndex) {
        if (parameterIndex < 0 || parameterIndex >= numParams) {
            throw new IndexOutOfBoundsException("Parameter index out of bounds: " + parameterIndex);
        }

        double[] marginalValues = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            marginalValues[i] = matrix.get(i, parameterIndex);
        }
        return new Sample(marginalValues);
    }

    /**
     * Expose la matrice dense sous-jacente pour les calculs algébriques globaux.
     */
    public DenseMatrix getUnderlyingMatrix() {
        return matrix;
    }
}