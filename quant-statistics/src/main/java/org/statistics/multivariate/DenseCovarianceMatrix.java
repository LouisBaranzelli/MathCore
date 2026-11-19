package org.statistics.multivariate;

import org.math.matrix.DenseMatrix;
import org.math.matrix.DenseSymmetricMatrix;
import org.math.matrix.Matrix;
import org.math.matrix.MatrixTools;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

public class DenseCovarianceMatrix extends DenseSymmetricMatrix implements CovarianceMatrix {

    public DenseCovarianceMatrix(double[][] matrix) {
        this(new DenseMatrix(matrix));
    }

    public DenseCovarianceMatrix(Matrix matrix) {
        this(validateAndExtractDimension(matrix), MatrixTools.extractUpperPacked(matrix));
    }

    public DenseCovarianceMatrix(int dimension, double[] packedValues) {
        super(dimension, packedValues);
        validateVariances();
    }


    @Override
    public Vector getVariances() {
        int n = getDimension();
        double[] vars = new double[n];
        for (int i = 0; i < n; i++) {
            vars[i] = get(i, i);
        }
        return new ArrayVector(vars);
    }

    @Override
    public Vector getStandardDeviations() {
        int n = getDimension();
        double[] stds = new double[n];
        for (int i = 0; i < n; i++) {
            stds[i] = Math.sqrt(get(i, i));
        }
        return new ArrayVector(stds);
    }

    @Override
    public CorrelationMatrix toCorrelationMatrix() {
        int n = getDimension();
        double[] stds = getStandardDeviations().toArray();
        double[] corrPacked = new double[MatrixTools.packedSize(n)];

        int idx = 0;
        for (int r = 0; r < n; r++) {
            double stdR = stds[r];
            // Diagonale r == c -> corrélation = 1.0
            corrPacked[idx++] = 1.0;

            for (int c = r + 1; c < n; c++) {
                double stdC = stds[c];
                double cov = get(r, c);

                double corr = cov / (stdR * stdC);
                corr = Math.max(-1.0, Math.min(1.0, corr)); // Clamping numérique

                corrPacked[idx++] = corr;
            }
        }
        return new DenseCorrelationMatrix(n, corrPacked);
    }



    private void validateVariances() {
        int n = getDimension();
        for (int i = 0; i < n; i++) {
            if (get(i, i) < 0.0) {
                throw new IllegalArgumentException(
                        String.format("Variance négative à l'index %d : %f", i, get(i, i))
                );
            }
        }
    }

    private static int validateAndExtractDimension(Matrix matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("La matrice source ne peut pas être nulle.");
        }
        int rows = matrix.rowCount();
        int cols = matrix.columnCount();
        if (rows <= 0 || rows != cols) {
            throw new IllegalArgumentException(
                    String.format("La matrice doit être carrée et non vide (%dx%d).", rows, cols)
            );
        }
        if (!MatrixTools.isSymetric(matrix, rows)) {
            throw new IllegalArgumentException("La matrice de covariance doit être symétrique.");
        }
        return rows;
    }
}
