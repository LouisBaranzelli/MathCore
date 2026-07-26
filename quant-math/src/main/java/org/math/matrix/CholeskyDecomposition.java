package org.math.matrix;

import java.util.Objects;

/**
 * Calcule la décomposition de Cholesky d'une matrice symétrique définie positive Σ = L * L^T.
 * La décomposition de Cholesky est une méthode d'algèbre linéaire qui permet de factoriser une matrice carrée $A$
 * en un produit de deux matrices triangulaires.
 * C'est en quelque sorte l'équivalent de la racine carrée d'une matrice.
 * <p>
 * Utilisée massivement pour :
 * 1. Le tirage de variables aléatoires corrélées (x = μ + L * z)
 * 2. Le calcul de la distance de Mahalanobis sans inverser directement la matrice.
 * 3. Le calcul du log-déterminant pour la densité gaussienne.
 * </p>
 */
public final class CholeskyDecomposition {

    private final Matrix L;             // Matrice triangulaire inférieure
    private final double logDeterminant; // ln(|Σ|) = 2 * Σ ln(L_ii)

    /**
     * Calcule la décomposition de Cholesky de la matrice A.
     *
     * @param A Matrice carrée, symétrique et définie positive.
     * @throws IllegalArgumentException si la matrice n'est pas carrée, pas symétrique ou pas définie positive.
     */
    public CholeskyDecomposition(Matrix A) {
        Objects.requireNonNull(A, "La matrice A ne peut pas être null.");

        if (!A.isSquare()) {
            throw new IllegalArgumentException("La matrice doit être carrée pour la décomposition de Cholesky.");
        }

        int n = A.rowCount();
        double[][] lData = new double[n][n];
        double logDetSum = 0.0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                for (int k = 0; k < j; k++) {
                    sum += lData[i][k] * lData[j][k];
                }

                if (i == j) {
                    double val = A.get(i, i) - sum;
                    // Vérification que la matrice est définie positive
                    if (val <= 0.0) {
                        throw new IllegalArgumentException(
                                "La matrice n'est pas définie positive (valeur non stricte à la diagonale " + i + " : " + val + ")."
                        );
                    }
                    lData[i][i] = Math.sqrt(val);
                    logDetSum += Math.log(lData[i][i]);
                } else {
                    lData[i][j] = (A.get(i, j) - sum) / lData[j][j];
                }
            }
        }

        this.L = new DenseMatrix(lData);
        // |Σ| = |L * L^T| = |L|^2 = (∏ L_ii)^2  =>  ln(|Σ|) = 2 * Σ ln(L_ii)
        this.logDeterminant = 2.0 * logDetSum;
    }

    /**
     * @return La matrice triangulaire inférieure L telle que Σ = L * L^T.
     */
    public Matrix getL() {
        return L;
    }

    /**
     * @return Le log-déterminant de la matrice d'origine : ln(|Σ|).
     */
    public double getLogDeterminant() {
        return logDeterminant;
    }

    /**
     * Résout numériquement L * y = b par substitution avant (Forward Substitution).
     * Très rapide : O(n²) au lieu de O(n³) pour une inversion classique.
     *
     * @param b Vecteur de taille n
     * @return Le vecteur y tel que L * y = b
     */
    public double[] solveLowerTriangular(double[] b) {
        int n = L.rowCount();
        if (b.length != n) {
            throw new IllegalArgumentException("Dimension du vecteur b incompatible.");
        }

        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < i; j++) {
                sum += L.get(i, j) * y[j];
            }
            y[i] = (b[i] - sum) / L.get(i, i);
        }
        return y;
    }

    /**
     * Calcule la norme au carré ||y||² = y^T * y où y est solution de L * y = b.
     * <p>
     * C'est la méthode clé pour calculer la distance de Mahalanobis :
     * b^T * Σ^-1 * b = b^T * (L * L^T)^-1 * b = (L^-1 * b)^T * (L^-1 * b) = ||y||²
     * </p>
     *
     * @param b Le vecteur de différence (x - μ)
     * @return La valeur scalaire (x - μ)^T * Σ^-1 * (x - μ)
     */
    public double solveLowerTriangularSquaredNorm(double[] b) {
        double[] y = solveLowerTriangular(b);
        double sumOfSquares = 0.0;
        for (double val : y) {
            sumOfSquares += val * val;
        }
        return sumOfSquares;
    }
}