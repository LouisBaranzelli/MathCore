package org.multivariate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.matrix.Matrix;
import org.math.matrix.MatrixFactory;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

class DenseCorrelationMatrixTest {

    private static final double EPSILON = 1e-8;

    @Nested
    @DisplayName("Tests de construction et validation")
    class ConstructionTests {

        @Test
        @DisplayName("Devrait créer une matrice de corrélation valide")
        void shouldCreateValidCorrelationMatrix() {
            // Matrice 2x2 : [[1.0, 0.5], [0.5, 1.0]] -> packed: [1.0, 0.5, 1.0]
            double[] packed = {1.0, 0.5, 1.0};

            DenseCorrelationMatrix matrix = assertDoesNotThrow(() -> new DenseCorrelationMatrix(2, packed));

            assertEquals(2, matrix.getDimension());
            assertEquals(1.0, matrix.get(0, 0), EPSILON);
            assertEquals(0.5, matrix.get(0, 1), EPSILON);
            assertEquals(0.5, matrix.get(1, 0), EPSILON);
            assertEquals(1.0, matrix.get(1, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait lever une exception si la diagonale n'est pas égale à 1")
        void shouldThrowExceptionWhenDiagonalIsNotOne() {
            // Diagonale invalide [0.9, 1.0]
            double[] packed = {0.9, 0.5, 1.0};

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new DenseCorrelationMatrix(2, packed)
            );
        }

        @Test
        @DisplayName("Devrait lever une exception si un terme hors-diagonale est hors de [-1, 1]")
        void shouldThrowExceptionWhenOffDiagonalIsOutOfBounds() {
            // Corrélation > 1.0
            double[] packedUpper = {1.0, 1.2, 1.0};
            assertThrows(IllegalArgumentException.class, () -> new DenseCorrelationMatrix(2, packedUpper));

            // Corrélation < -1.0
            double[] packedLower = {1.0, -1.05, 1.0};
            assertThrows(IllegalArgumentException.class, () -> new DenseCorrelationMatrix(2, packedLower));
        }

        @Test
        @DisplayName("isValid() doit échouer si la diagonale varie selon la tolérance")
        void shouldHonorCustomToleranceInIsValid() {
            double[] packed = {1.0001, 0.3, 1.0};
            assertThrows(IllegalArgumentException.class, () -> new DenseCorrelationMatrix(2, packed));
        }
    }

    @Nested
    @DisplayName("Tests de semi-définie positivité (isPositiveSemiDefinite)")
    class PositiveSemiDefiniteTests {

        @Test
        @DisplayName("Devrait retourner true pour une matrice SDP valide")
        void shouldReturnTrueForValidPSDMatrix() {
            // Matrice 3x3 valide et SDP
            // [ 1.0,  0.5,  0.2 ]
            // [ 0.5,  1.0,  0.3 ]
            // [ 0.2,  0.3,  1.0 ]
            double[] packed = {
                    1.0, 0.5, 0.2,
                    1.0, 0.3,
                    1.0
            };
            DenseCorrelationMatrix matrix = new DenseCorrelationMatrix(3, packed);

            assertTrue(matrix.isPositiveSemiDefinite());
        }

        @Test
        @DisplayName("Devrait être équivalente à MatrixFactory.identity() pour le cas identité")
        void shouldBeEquivalentToMatrixFactoryIdentity() {
            double[] packedIdentity = {
                    1.0, 0.0, 0.0,
                    1.0, 0.0,
                    1.0
            };
            DenseCorrelationMatrix corrIdentity = new DenseCorrelationMatrix(3, packedIdentity);
            Matrix factoryIdentity = MatrixFactory.identity(3);

            assertTrue(corrIdentity.isPositiveSemiDefinite());
            assertEquals(factoryIdentity.get(0, 0), corrIdentity.get(0, 0), EPSILON);
            assertEquals(factoryIdentity.get(0, 1), corrIdentity.get(0, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait retourner false pour une matrice incohérente non-SDP")
        void shouldReturnFalseForNonPSDMatrix() {
            // Matrice non-SDP (ex: corrélation croisée contradictoire)
            // A et B très corrélés (+0.9), B et C très corrélés (+0.9), mais A et C très anticorrélés (-0.9)
            // [ 1.0,  0.9, -0.9 ]
            // [ 0.9,  1.0,  0.9 ]
            // [-0.9,  0.9,  1.0 ]
            double[] packed = {
                    1.0,  0.9, -0.9,
                    1.0,  0.9,
                    1.0
            };
            DenseCorrelationMatrix matrix = new DenseCorrelationMatrix(3, packed);

            assertFalse(matrix.isPositiveSemiDefinite());
        }
    }

    @Nested
    @DisplayName("Tests de conversion vers CovarianceMatrix")
    class ToCovarianceMatrixTests {

        @Test
        @DisplayName("Devrait convertir correctement en CovarianceMatrix")
        void shouldConvertToCovarianceMatrix() {
            // Matrice 2x2 : corr = 0.5
            double[] packedCorr = {1.0, 0.5, 1.0};
            DenseCorrelationMatrix corrMatrix = new DenseCorrelationMatrix(2, packedCorr);

            // Écarts-types : sigma_1 = 2.0, sigma_2 = 3.0
            Vector stds = new ArrayVector(new double[]{2.0, 3.0});

            CovarianceMatrix covMatrix = corrMatrix.toCovarianceMatrix(stds);

            // Cov(0,0) = 1.0 * 2.0 * 2.0 = 4.0
            assertEquals(4.0, covMatrix.get(0, 0), EPSILON);

            // Cov(0,1) = 0.5 * 2.0 * 3.0 = 3.0
            assertEquals(3.0, covMatrix.get(0, 1), EPSILON);
            assertEquals(3.0, covMatrix.get(1, 0), EPSILON);

            // Cov(1,1) = 1.0 * 3.0 * 3.0 = 9.0
            assertEquals(9.0, covMatrix.get(1, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait valider la nullité des covariances hors-diagonale pour un vecteur d'écarts-types nul")
        void shouldProduceZeroCovarianceWhenStdsAreZero() {
            double[] packedCorr = {1.0, 0.8, 1.0};
            DenseCorrelationMatrix corrMatrix = new DenseCorrelationMatrix(2, packedCorr);

            Vector zeroStds = new ArrayVector(new double[]{0.0, 0.0});
            CovarianceMatrix covMatrix = corrMatrix.toCovarianceMatrix(zeroStds);

            assertEquals(0.0, covMatrix.get(0, 0), EPSILON);
            assertEquals(0.0, covMatrix.get(0, 1), EPSILON);
        }

        @Test
        @DisplayName("Devrait lever une exception si les dimensions du vecteur d'écarts-types ne correspondent pas")
        void shouldThrowExceptionWhenVectorDimensionMismatch() {
            double[] packedCorr = {1.0, 0.5, 1.0};
            DenseCorrelationMatrix corrMatrix = new DenseCorrelationMatrix(2, packedCorr);

            Vector invalidStds = new ArrayVector(new double[]{2.0, 3.0, 4.0});

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> corrMatrix.toCovarianceMatrix(invalidStds)
            );
            assertTrue(exception.getMessage().contains("Incohérence de dimensions"));
        }

        @Test
        @DisplayName("Devrait lever une exception si un écart-type est négatif")
        void shouldThrowExceptionWhenNegativeStandardDeviation() {
            double[] packedCorr = {1.0, 0.5, 1.0};
            DenseCorrelationMatrix corrMatrix = new DenseCorrelationMatrix(2, packedCorr);

            Vector negativeStds = new ArrayVector(new double[]{2.0, -1.0});

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> corrMatrix.toCovarianceMatrix(negativeStds)
            );
            assertTrue(exception.getMessage().contains("écart-type ne peut pas être négatif"));
        }
    }
}