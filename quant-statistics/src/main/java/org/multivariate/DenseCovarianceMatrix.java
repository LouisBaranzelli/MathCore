package org.multivariate;

import org.math.matrix.DenseSymmetricMatrix;
import org.math.matrix.MatrixTools;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

public class DenseCovarianceMatrix extends DenseSymmetricMatrix implements CovarianceMatrix {


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
}
