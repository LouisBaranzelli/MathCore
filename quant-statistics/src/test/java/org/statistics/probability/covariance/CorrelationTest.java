package org.statistics.probability.covariance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationTest {

    private static final double EPSILON = 1e-8;

    @Nested
    @DisplayName("Tests de Correlation.of(Vector, Vector)")
    class BivariateCorrelationTests {

        @Test
        @DisplayName("Devrait calculer la corrélation de Pearson entre deux vecteurs")
        void shouldCalculateCorrelationForTwoVectors() {
            // X = [1, 2, 3, 4, 5], Y = [2, 4, 5, 4, 5]
            // Cov(X, Y) = 1.5, Var(X) = 2.5 (sigmaX = sqrt(2.5)), Var(Y) = 1.5 (sigmaY = sqrt(1.5))
            // Corr(X, Y) = 1.5 / sqrt(2.5 * 1.5) = 1.5 / sqrt(3.75) ≈ 0.774596669
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector y = new ArrayVector(new double[]{2.0, 4.0, 5.0, 4.0, 5.0});

            double corr = Correlation.of(x, y);

            assertEquals(1.5 / Math.sqrt(3.75), corr, EPSILON);
        }

        @Test
        @DisplayName("Devrait retourner 1.0 pour un vecteur corrélé avec lui-même")
        void shouldReturnOneWhenCalculatedWithSameVector() {
            Vector x = new ArrayVector(new double[]{1.0, 3.0, 5.0, 7.0});

            double corr = Correlation.of(x, x);

            assertEquals(1.0, corr, EPSILON);
        }

        @Test
        @DisplayName("Devrait retourner -1.0 pour deux vecteurs parfaitement inversement linéaires")
        void shouldReturnMinusOneForPerfectNegativeCorrelation() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector y = new ArrayVector(new double[]{-2.0, -4.0, -6.0});

            double corr = Correlation.of(x, y);

            assertEquals(-1.0, corr, EPSILON);
        }

        @Test
        @DisplayName("Devrait lever une exception si les tailles de vecteurs diffèrent")
        void shouldThrowExceptionForDimensionMismatch() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0});
            Vector y = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            assertThrows(IllegalArgumentException.class, () -> Correlation.of(x, y));
        }
    }

    @Nested
    @DisplayName("Tests de Correlation.of(Vector...) et Correlation.of(boolean, Vector...)")
    class MultivariateCorrelationTests {

        @Test
        @DisplayName("Devrait générer une matrice 3x3 avec des valeurs de corrélation toutes distinctes")
        void shouldCreateMatrixOfCorrectDimensionsAndDistinctValues() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector v2 = new ArrayVector(new double[]{2.0, 8.0, 11.0});
            Vector v3 = new ArrayVector(new double[]{2.0, 3.0, 7.0});

            CorrelationMatrix matrix = Correlation.of(v1, v2, v3);

            assertEquals(3, matrix.getDimension());

            // Valeurs théoriques dérivées des variances et covariances
            double rho12 = 4.5 / Math.sqrt(1.0 * 21.0);
            double rho13 = 2.5 / Math.sqrt(1.0 * 7.0);
            double rho23 = 10.5 / Math.sqrt(21.0 * 7.0);

            double[][] expected = {
                    { 1.0,    rho12,  rho13 },
                    { rho12,  1.0,    rho23 },
                    { rho13,  rho23,  1.0   }
            };

            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    assertEquals(expected[r][c], matrix.get(r, c), EPSILON,
                            String.format("Valeur incorrecte à la position (%d, %d)", r, c));
                }
            }
        }

        @Test
        @DisplayName("La matrice de corrélation doit être identique avec ou sans correction de biais (biasCorrected)")
        void shouldProduceIdenticalMatrixRegardlessOfBiasCorrection() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector v2 = new ArrayVector(new double[]{2.0, 8.0, 11.0});

            // La corrélation normalise par les écarts-types, annulant le facteur N/(N-1)
            CorrelationMatrix withBias = Correlation.of(true, v1, v2);
            CorrelationMatrix withoutBias = Correlation.of(false, v1, v2);

            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 2; c++) {
                    assertEquals(withBias.get(r, c), withoutBias.get(r, c), EPSILON);
                }
            }
        }

        @Test
        @DisplayName("Devrait lever une exception s'il y a moins de 2 vecteurs")
        void shouldThrowExceptionWhenLessThanTwoVectors() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            assertThrows(IllegalArgumentException.class, () -> Correlation.of(v1));
            assertThrows(IllegalArgumentException.class, () -> Correlation.of(true, (Vector[]) null));
        }

        @Test
        @DisplayName("Devrait lever une exception si les vecteurs n'ont pas la même taille")
        void shouldThrowExceptionWhenVectorsHaveDifferentSizes() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector v2 = new ArrayVector(new double[]{4.0, 5.0});

            assertThrows(IllegalArgumentException.class, () -> Correlation.of(v1, v2));
        }
    }
}