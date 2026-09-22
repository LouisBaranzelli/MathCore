package org.multivariate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.multivariate.Covariance;
import org.multivariate.CovarianceMatrix;

import static org.junit.jupiter.api.Assertions.*;

class CovarianceTest {

    private static final double EPSILON = 1e-8;

    @Nested
    @DisplayName("Tests de Covariance.of(Vector, Vector)")
    class BivariateCovarianceTests {

        @Test
        @DisplayName("Devrait calculer la covariance correcte pour deux vecteurs de même taille")
        void shouldCalculateCovarianceForTwoVectors() {
            // X = [1, 2, 3, 4, 5], meanX = 3.0
            // Y = [2, 4, 5, 4, 5], meanY = 4.0
            // (X-meanX)*(Y-meanY) = [-2*-2, -1*0, 0*1, 1*0, 2*1] = [4, 0, 0, 0, 2] -> sum = 6.0
            // Cov = 6.0 / (5 - 1) = 1.5
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector y = new ArrayVector(new double[]{2.0, 4.0, 5.0, 4.0, 5.0});

            double cov = Covariance.of(x, y);

            assertEquals(1.5, cov, EPSILON);
        }

        @Test
        @DisplayName("Devrait retourner la variance du vecteur si on calcule Covariance.of(x, x)")
        void shouldMatchVarianceWhenCalculatedWithSameVector() {
            Vector x = new ArrayVector(new double[]{1.0, 3.0, 5.0, 7.0});
            // mean = 4.0, sumSq = 9 + 1 + 1 + 9 = 20. Sample Var = 20 / 3 = 6.66666667

            double cov = Covariance.of(x, x);

            assertEquals(20.0 / 3.0, cov, EPSILON);
        }

        @Test
        @DisplayName("Devrait retourner 0.0 si la taille du vecteur est <= 1")
        void shouldReturnZeroForVectorSizeOneOrLess() {
            Vector x = new ArrayVector(new double[]{42.0});
            Vector y = new ArrayVector(new double[]{10.0});

            assertEquals(0.0, Covariance.of(x, y), EPSILON);
        }

        @Test
        @DisplayName("Devrait lever une exception si les tailles de vecteurs different")
        void shouldThrowExceptionForDimensionMismatch() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0});
            Vector y = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> Covariance.of(x, y)
            );
            assertTrue(exception.getMessage().contains("Vector must have the same size."));
        }
    }

    @Nested
    @DisplayName("Tests de Covariance.of(Vector...) et Covariance.of(boolean, Vector...)")
    class MultivariateCovarianceTests {

        @Test
        @DisplayName("Devrait calculer une matrice de covariance avec biais corrigé (par défaut)")
        void shouldCalculateDefaultSampleCovarianceMatrix() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector v2 = new ArrayVector(new double[]{2.0, 4.0, 5.0, 4.0, 5.0});

            CovarianceMatrix matrix = Covariance.of(new Vector[] { v1, v2 });

            assertEquals(2, matrix.getDimension());
            // Cov(v1, v1) = 2.5
            assertEquals(2.5, matrix.get(0, 0), EPSILON);
            // Cov(v1, v2) = 1.5
            assertEquals(1.5, matrix.get(0, 1), EPSILON);
            assertEquals(1.5, matrix.get(1, 0), EPSILON);
            // Cov(v2, v2) = 1.5
            assertEquals(1.5, matrix.get(1, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait calculer une matrice de covariance sans biais (population, biasCorrected = false)")
        void shouldCalculatePopulationCovarianceMatrix() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}); // N = 5
            Vector v2 = new ArrayVector(new double[]{2.0, 4.0, 5.0, 4.0, 5.0});

            // Division par N (5) au lieu de N-1 (4)
            CovarianceMatrix matrix = Covariance.of(false, v1, v2);

            // Cov_pop(v1, v1) = 10.0 / 5 = 2.0
            assertEquals(2.0, matrix.get(0, 0), EPSILON);
            // Cov_pop(v1, v2) = 6.0 / 5 = 1.2
            assertEquals(1.2, matrix.get(0, 1), EPSILON);
            assertEquals(1.2, matrix.get(1, 0), EPSILON);
            // Cov_pop(v2, v2) = 6.0 / 5 = 1.2
            assertEquals(1.2, matrix.get(1, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait générer une matrice 3x3 avec des valeurs de covariance toutes distinctes")
        void shouldCreateMatrixOfCorrectDimensionsAndDistinctValues() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector v2 = new ArrayVector(new double[]{2.0, 8.0, 11.0});
            Vector v3 = new ArrayVector(new double[]{2.0, 3.0, 7.0});

            CovarianceMatrix matrix = Covariance.of(v1, v2, v3);

            assertEquals(3, matrix.getDimension());

            double[][] expected = {
                    { 1.0,  4.5,  2.5},
                    { 4.5, 21.0, 10.5},
                    { 2.5, 10.5,  7.0}
            };

            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    assertEquals(expected[r][c], matrix.get(r, c), EPSILON,
                            String.format("Valeur incorrecte à la position (%d, %d)", r, c));
                }
            }
        }

        @Test
        @DisplayName("Devrait lever une exception s'il y a moins de 2 vecteurs")
        void shouldThrowExceptionWhenLessThanTwoVectors() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            assertThrows(IllegalArgumentException.class, () -> Covariance.of(v1));
            assertThrows(IllegalArgumentException.class, () -> Covariance.of(true, (Vector[]) null));
        }

        @Test
        @DisplayName("Devrait lever une exception si les vecteurs n'ont pas la même taille")
        void shouldThrowExceptionWhenVectorsHaveDifferentSizes() {
            Vector v1 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector v2 = new ArrayVector(new double[]{4.0, 5.0});

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> Covariance.of(v1, v2)
            );
        }
    }
}