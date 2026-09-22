package org.multivariate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

class DenseCovarianceMatrixTest {

    private static final double EPSILON = 1e-8;

    @Nested
    @DisplayName("Tests de construction et de factory")
    class ConstructionTests {

        @Test
        @DisplayName("Devrait instancier une matrice de covariance valide via le tableau compacté")
        void shouldCreateValidCovarianceMatrixFromPackedArray() {
            // Matrice 2x2 : [[4.0, 3.0], [3.0, 9.0]] -> packed: [4.0, 3.0, 9.0]
            double[] packed = {4.0, 3.0, 9.0};
            DenseCovarianceMatrix cov = assertDoesNotThrow(() -> new DenseCovarianceMatrix(2, packed));

            assertEquals(2, cov.getDimension());
            assertEquals(4.0, cov.get(0, 0), EPSILON);
            assertEquals(3.0, cov.get(0, 1), EPSILON);
            assertEquals(3.0, cov.get(1, 0), EPSILON);
            assertEquals(9.0, cov.get(1, 1), EPSILON);
        }


        @Test
        @DisplayName("Devrait lever une exception si une variance (diagonale) est négative")
        void shouldThrowExceptionWhenVarianceIsNegative() {
            // Variance négative (-1.0) à l'index (0,0)
            double[] packed = {-1.0, 0.5, 4.0};

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new DenseCovarianceMatrix(2, packed)
            );
            assertTrue(exception.getMessage().contains("Variance négative à l'index 0"));
        }
    }

    @Nested
    @DisplayName("Tests des accesseurs de variances et écarts-types")
    class ExtractionTests {

        @Test
        @DisplayName("Devrait extraire les variances (diagonale)")
        void shouldExtractVariances() {
            double[] packed = {
                    16.0, 2.0, 1.0,
                    25.0, 4.0,
                    36.0
            };
            DenseCovarianceMatrix cov = new DenseCovarianceMatrix(3, packed);

            Vector vars = cov.getVariances();
            assertEquals(3, vars.size());
            assertEquals(16.0, vars.getValue(0), EPSILON);
            assertEquals(25.0, vars.getValue(1), EPSILON);
            assertEquals(36.0, vars.getValue(2), EPSILON);
        }

        @Test
        @DisplayName("Devrait extraire les écarts-types (racine carrée des variances)")
        void shouldExtractStandardDeviations() {
            double[] packed = {
                    16.0, 2.0, 1.0,
                    25.0, 4.0,
                    36.0
            };
            DenseCovarianceMatrix cov = new DenseCovarianceMatrix(3, packed);

            Vector stds = cov.getStandardDeviations();
            assertEquals(3, stds.size());
            assertEquals(4.0, stds.getValue(0), EPSILON);
            assertEquals(5.0, stds.getValue(1), EPSILON);
            assertEquals(6.0, stds.getValue(2), EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests de conversion vers CorrelationMatrix")
    class ToCorrelationMatrixTests {

        @Test
        @DisplayName("Devrait convertir une matrice de covariance en matrice de corrélation valide")
        void shouldConvertToCorrelationMatrix() {
            // Var(1) = 4.0 (std=2.0), Var(2) = 9.0 (std=3.0), Cov(1,2) = 3.0
            // Corr(1,2) = 3.0 / (2.0 * 3.0) = 0.5
            double[] packed = {4.0, 3.0, 9.0};
            DenseCovarianceMatrix cov = new DenseCovarianceMatrix(2, packed);

            CorrelationMatrix corr = cov.toCorrelationMatrix();

            assertEquals(1.0, corr.get(0, 0), EPSILON);
            assertEquals(0.5, corr.get(0, 1), EPSILON);
            assertEquals(0.5, corr.get(1, 0), EPSILON);
            assertEquals(1.0, corr.get(1, 1), EPSILON);
            assertTrue(corr.isValid(1e-8));
        }

        @Test
        @DisplayName("Devrait appliquer le clamping aux valeurs limites de corrélation")
        void shouldClampCorrelationValues() {
            // Test de précision numérique où la corrélation approche 1.0
            double[] packed = {1.0, 1.0000000000000002, 1.0};
            DenseCovarianceMatrix cov = new DenseCovarianceMatrix(2, packed);

            CorrelationMatrix corr = assertDoesNotThrow(cov::toCorrelationMatrix);
            assertEquals(1.0, corr.get(0, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait correspondre à l'identité pour une covariance diagonale égale")
        void shouldProduceIdentityCorrelationForDiagonalCovariance() {
            double[] packedDiagonalCov = {
                    4.0, 0.0, 0.0,
                    9.0, 0.0,
                    16.0
            };
            DenseCovarianceMatrix cov = new DenseCovarianceMatrix(3, packedDiagonalCov);

            CorrelationMatrix corr = cov.toCorrelationMatrix();

            // Verification directe des valeurs
            assertEquals(1.0, corr.get(0, 0), EPSILON);
            assertEquals(0.0, corr.get(0, 1), EPSILON);
            assertEquals(0.0, corr.get(0, 2), EPSILON);
            assertEquals(1.0, corr.get(1, 1), EPSILON);
            assertEquals(0.0, corr.get(1, 2), EPSILON);
            assertEquals(1.0, corr.get(2, 2), EPSILON);
        }
    }
}