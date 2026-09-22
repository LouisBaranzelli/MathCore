package org.multivariate;

import org.math.matrix.DenseSymmetricMatrix;
import org.math.vector.Vector;

public class DenseCorrelationMatrix extends DenseSymmetricMatrix implements CorrelationMatrix {

    private static final double DEFAULT_TOLERANCE = 1e-8;

    /**
     * Constructeur à partir d'un tableau compacté (Packed Storage) de taille N*(N+1)/2.
     */
    public DenseCorrelationMatrix(int dimension, double[] packedValues) {
        super(dimension, packedValues);
        if (!isValid(DEFAULT_TOLERANCE)) {
            throw new IllegalArgumentException(
                    "Les valeurs fournies ne forment pas une matrice de corrélation valide (diagonale != 1 ou valeurs hors [-1, 1])."
            );
        }
    }


    @Override
    public boolean isPositiveSemiDefinite() {
        int n = getDimension();
        double[][] a = to2DArray();

        // Test de Cholesky modifié pour vérifier la semi-définie positivité (valeurs propres >= 0)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = a[i][j];
                for (int k = 0; k < j; k++) {
                    sum -= a[i][k] * a[j][k];
                }
                if (i == j) {
                    if (sum < -DEFAULT_TOLERANCE) {
                        return false; // Pivot négatif -> non SDP
                    }
                    a[i][i] = sum <= 0 ? 0 : Math.sqrt(sum);
                } else {
                    a[i][j] = (a[i][i] == 0) ? 0 : sum / a[i][i];
                }
            }
        }
        return true;
    }


    @Override
    public CovarianceMatrix toCovarianceMatrix(Vector standardDeviations) {
        int n = getDimension();
        if (standardDeviations.size() != n) {
            throw new IllegalArgumentException(
                    String.format("Incohérence de dimensions : matrice %dx%d, vecteur écarts-types %d",
                            n, n, standardDeviations.size())
            );
        }

        double[] stds = standardDeviations.toArray();
        double[] covPacked = new double[n * (n + 1) / 2];

        int idx = 0;
        for (int r = 0; r < n; r++) {
            double stdR = stds[r];
            if (stdR < 0) {
                throw new IllegalArgumentException("L'écart-type ne peut pas être négatif à l'index " + r);
            }
            for (int c = r; c < n; c++) {
                double stdC = stds[c];
                // cov_ij = corr_ij * sigma_i * sigma_j
                covPacked[idx++] = get(r, c) * stdR * stdC;
            }
        }
        return new DenseCovarianceMatrix(n, covPacked);
    }

    private double[][] to2DArray() {
        int n = getDimension();
        double[][] array = new double[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                array[r][c] = get(r, c);
            }
        }
        return array;
    }


}