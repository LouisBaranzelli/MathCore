package org.math.matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CholeskyDecompositionTest {

    private static final double EPSILON = 1e-9;

    @Nested
    @DisplayName("Tests de décomposition valide (Cas nominal)")
    class NominalCases {

        @Test
        @DisplayName("Doit correctement décomposer une matrice 2x2 symétrique définie positive")
        void testCholesky2x2() {
            // A = [ 4  12 ]
            //     [ 12 45 ]
            // Matrice L théorique : [ 2  0 ]
            //                       [ 6  3 ]
            double[][] data = {
                    {4.0, 12.0},
                    {12.0, 45.0}
            };
            Matrix A = new DenseMatrix(data);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);
            Matrix L = cholesky.getL();

            // Vérification des éléments de L
            assertEquals(2.0, L.get(0, 0), EPSILON);
            assertEquals(0.0, L.get(0, 1), EPSILON);
            assertEquals(6.0, L.get(1, 0), EPSILON);
            assertEquals(3.0, L.get(1, 1), EPSILON);

            // Vérification de L * L^T = A
            assertMatrixEquals(A, reconstructA(L));
        }

        @Test
        @DisplayName("Doit correctement décomposer une matrice 3x3 symétrique définie positive")
        void testCholesky3x3() {
            // Matrice de covariance classique
            double[][] data = {
                    {25.0,  15.0,  -5.0},
                    {15.0,  18.0,   0.0},
                    {-5.0,   0.0,  11.0}
            };
            Matrix A = new DenseMatrix(data);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);
            Matrix L = cholesky.getL();

            // Vérification que L est bien triangulaire inférieure
            assertTrue(L.get(0, 1) == 0.0);
            assertTrue(L.get(0, 2) == 0.0);
            assertTrue(L.get(1, 2) == 0.0);

            // Reconstitution L * L^T == A
            assertMatrixEquals(A, reconstructA(L));
        }

        @Test
        @DisplayName("Doit calculer le log-déterminant avec précision")
        void testLogDeterminant() {
            // Matrice A 2x2 avec |A| = (4*45) - (12*12) = 180 - 144 = 36
            // ln(|A|) = ln(36) ≈ 3.58351893845611
            double[][] data = {
                    {4.0, 12.0},
                    {12.0, 45.0}
            };
            Matrix A = new DenseMatrix(data);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);

            double expectedLogDet = Math.log(36.0);
            assertEquals(expectedLogDet, cholesky.getLogDeterminant(), EPSILON);
        }

        @Test
        @DisplayName("Doit correctement résoudre L * y = b par substitution avant")
        void testSolveLowerTriangular() {
            // L = [ 2  0 ]
            //     [ 6  3 ]
            // Soit b = [ 4, 18 ]
            // L * y = b  => 2*y1 = 4 => y1 = 2
            //            => 6*y1 + 3*y2 = 18 => 12 + 3*y2 = 18 => y2 = 2
            double[][] data = {
                    {4.0, 12.0},
                    {12.0, 45.0}
            };
            Matrix A = new DenseMatrix(data);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);
            double[] b = {4.0, 18.0};

            double[] y = cholesky.solveLowerTriangular(b);

            assertEquals(2.0, y[0], EPSILON);
            assertEquals(2.0, y[1], EPSILON);
        }

        @Test
        @DisplayName("Doit calculer la norme au carré de y (distance de Mahalanobis)")
        void testSolveLowerTriangularSquaredNorm() {
            // Avec y = [ 2, 2 ], ||y||² = 2² + 2² = 8
            double[][] data = {
                    {4.0, 12.0},
                    {12.0, 45.0}
            };
            Matrix A = new DenseMatrix(data);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);
            double[] b = {4.0, 18.0};

            double squaredNorm = cholesky.solveLowerTriangularSquaredNorm(b);

            assertEquals(8.0, squaredNorm, EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests de validation et d'exceptions")
    class ExceptionCases {

        @Test
        @DisplayName("Lève NullPointerException si la matrice fournie est null")
        void testNullMatrix() {
            assertThrows(NullPointerException.class, () -> new CholeskyDecomposition(null));
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la matrice n'est pas carrée")
        void testNonSquareMatrix() {
            // Matrice 2x3
            double[][] data = {
                    {1.0, 2.0, 3.0},
                    {4.0, 5.0, 6.0}
            };
            Matrix nonSquare = new DenseMatrix(data);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new CholeskyDecomposition(nonSquare)
            );
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la matrice n'est pas définie positive (diagonale négative/nulle)")
        void testNonPositiveDefiniteMatrix() {
            // Matrice non définie positive (déterminant négatif)
            double[][] data = {
                    {1.0, 2.0},
                    {2.0, 1.0}
            };
            Matrix notPositiveDefinite = new DenseMatrix(data);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new CholeskyDecomposition(notPositiveDefinite)
            );
        }

        @Test
        @DisplayName("Lève IllegalArgumentException lors de solveLowerTriangular si la taille du vecteur b ne correspond pas")
        void testSolveWithInvalidVectorDimension() {
            double[][] data = {
                    {4.0, 12.0},
                    {12.0, 45.0}
            };
            Matrix A = new DenseMatrix(data);
            CholeskyDecomposition cholesky = new CholeskyDecomposition(A);

            // Vecteur de taille 3 au lieu de 2
            double[] invalidB = {1.0, 2.0, 3.0};

            assertThrows(
                    IllegalArgumentException.class,
                    () -> cholesky.solveLowerTriangular(invalidB)
            );
        }
    }

    // --- Méthodes utilitaires pour les tests ---

    /**
     * Reconstruit A = L * L^T pour valider la décomposition.
     */
    private Matrix reconstructA(Matrix L) {
        int n = L.rowCount();
        double[][] result = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += L.get(i, k) * L.get(j, k); // L(i,k) * L^T(k,j) = L(i,k) * L(j,k)
                }
                result[i][j] = sum;
            }
        }
        return new DenseMatrix(result);
    }

    private void assertMatrixEquals(Matrix expected, Matrix actual) {
        assertEquals(expected.rowCount(), actual.rowCount(), "Nombre de lignes différent");
        assertEquals(expected.columnCount(), actual.columnCount(), "Nombre de colonnes différent");

        for (int i = 0; i < expected.rowCount(); i++) {
            for (int j = 0; j < expected.columnCount(); j++) {
                assertEquals(
                        expected.get(i, j),
                        actual.get(i, j),
                        EPSILON,
                        "Erreur à la position (" + i + ", " + j + ")"
                );
            }
        }
    }
}