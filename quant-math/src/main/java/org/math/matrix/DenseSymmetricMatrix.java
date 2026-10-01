package org.math.matrix;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DenseSymmetricMatrix extends AbstractMatrix implements SymmetricMatrix {

    private final int dimension;
    private final double[] packedValues;
    private static Logger logger = LoggerFactory.getLogger(DenseMatrix.class);

    public DenseSymmetricMatrix(int dimension, double[] packedValues) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        int expectedLength = dimension * (dimension + 1) / 2;
        if (packedValues.length != expectedLength) {
            throw new IllegalArgumentException(
                    String.format("Packed array length must be %d for dimension %d, got %d",
                            expectedLength, dimension, packedValues.length)
            );
        }
        this.dimension = dimension;
        this.packedValues = packedValues.clone();
    }


    @Override public int getDimension() { return dimension; }

    @Override
    public Matrix invert() {
        int n = getDimension();
        int packedSize = MatrixTools.packedSize(n);
        double[] work = new double[packedSize];

        // --- ÉTAPE 1 : Factorisation de Cholesky A = L * L^T ---
        for (int i = 0; i < n; i++) {
            int rowOffset = i * (i + 1) / 2;

            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                int jRowOffset = j * (j + 1) / 2;

                for (int k = 0; k < j; k++) {
                    sum += work[rowOffset + k] * work[jRowOffset + k];
                }

                if (i == j) {
                    double val = get(i, i) - sum;
                    if (val <= 0.0) {
                        logger.info("La matrice n'est pas définie positive (échec Cholesky à l'indice {} ).", i);
                        return super.invert();

                    }
                    work[rowOffset + j] = Math.sqrt(val);
                } else {
                    work[rowOffset + j] = (get(i, j) - sum) / work[jRowOffset + j];
                }
            }
        }

        // --- ÉTAPE 2 : Inversion de L (L^-1) in-place dans le triangle inférieur de 'work' ---
        for (int i = 0; i < n; i++) {
            int rowOffset = i * (i + 1) / 2;
            double lDiag = work[rowOffset + i];

            work[rowOffset + i] = 1.0 / lDiag;

            for (int j = 0; j < i; j++) {
                double sum = 0.0;
                for (int k = j; k < i; k++) {
                    int kRowOffset = k * (k + 1) / 2;
                    sum += work[rowOffset + k] * work[kRowOffset + j];
                }
                work[rowOffset + j] = -sum / lDiag;
            }
        }

        // --- ÉTAPE 3 : Reconstruction de A^-1 = (L^-1)^T * L^-1 ---
        // Écriture du résultat dans un nouveau tableau packed 1D (triangle supérieur)
        double[] invPacked = new double[packedSize];

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                double sum = 0.0;

                // Produit scalaire des colonnes i et j de L^-1 (qui sont les lignes de (L^-1)^T)
                for (int k = j; k < n; k++) {
                    int kRowOffset = k * (k + 1) / 2;
                    sum += work[kRowOffset + i] * work[kRowOffset + j];
                }

                invPacked[MatrixTools.upperTriangleIndex(i, j, n)] = sum;
            }
        }

        return new DenseSymmetricMatrix(n, invPacked);
    }


    @Override public int rowCount() { return dimension; }
    @Override public int columnCount() { return dimension; }

    @Override
    public double get(int row, int col) {
        checkIndices(row, col);
        int r = Math.min(row, col);
        int c = Math.max(row, col);
        int index = r * dimension - (r * (r - 1)) / 2 + (c - r);
        return packedValues[index];
    }


    @Override
    public Matrix add(Matrix other) {
        if (other instanceof SymmetricMatrix symOther) {
            checkSameDimensions(other);
            double[] resultPacked = new double[packedValues.length];
            int index = 0;
            for (int r = 0; r < dimension; r++) {
                for (int c = r; c < dimension; c++) {
                    resultPacked[index] = this.packedValues[index] + symOther.get(r, c);
                    index++;
                }
            }
            return new DenseSymmetricMatrix(dimension, resultPacked);
        }
        return super.add(other);
    }

    @Override
    public Matrix subtract(Matrix other) {
        if (other instanceof SymmetricMatrix symOther) {
            checkSameDimensions(other);
            double[] resultPacked = new double[packedValues.length];
            int index = 0;
            for (int r = 0; r < dimension; r++) {
                for (int c = r; c < dimension; c++) {
                    resultPacked[index] = this.packedValues[index] - symOther.get(r, c);
                    index++;
                }
            }
            return new DenseSymmetricMatrix(dimension, resultPacked);
        }
        return super.subtract(other);
    }

    @Override
    public Matrix multiply(double scalar) {
        double[] resultPacked = new double[packedValues.length];
        for (int i = 0; i < packedValues.length; i++) {
            resultPacked[i] = this.packedValues[i] * scalar;
        }
        return new DenseSymmetricMatrix(dimension, resultPacked);
    }

    @Override
    public Vector getColumn(int col) {
        return getRow(col);
    }

    private void checkIndices(int row, int col) {
        if (row < 0 || row >= dimension || col < 0 || col >= dimension) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for dimension %d", row, col, dimension)
            );
        }
    }
}