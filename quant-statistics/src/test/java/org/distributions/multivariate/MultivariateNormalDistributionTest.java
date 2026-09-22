package org.distributions.multivariate;

import org.distributions.multivariate.MultivariateNormalDistribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MultivariateNormalDistributionTest {

    private static final double EPSILON = 1e-6;

    private Vector mean2D;
    private Matrix cov2D;
    private MultivariateNormalDistribution dist2D;

    @BeforeEach
    void setUp() {
        // μ = [1.0, 2.0]^T
        mean2D = new ArrayVector(new double[]{1.0, 2.0});

        // Σ = [ 2.0  0.5 ]
        //     [ 0.5  1.0 ]
        double[][] covData = {
                {2.0, 0.5},
                {0.5, 1.0}
        };
        cov2D = new DenseMatrix(covData);

        // Fixer le seed du Random pour rendre les tests 100% déterministes
        dist2D = new MultivariateNormalDistribution(mean2D, cov2D, new Random(42));
    }

    @Nested
    @DisplayName("Tests de getters et de propriétés de base")
    class BasicPropertiesTests {

        @Test
        @DisplayName("Doit retourner la bonne dimension et les bonnes métriques initiales")
        void testProperties() {
            assertEquals(2, dist2D.getDimension());
            assertEquals(1.0, dist2D.getMean().getValue(0), EPSILON);
            assertEquals(2.0, dist2D.getMean().getValue(1), EPSILON);
            assertEquals(2.0, dist2D.getCovariance().get(0, 0), EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests de Calcul de Densité et Log-Densité")
    class DensityTests {

        @Test
        @DisplayName("La densité doit être maximale à la moyenne x = μ")
        void testDensityAtMean() {
            double[] xMean = {1.0, 2.0};
            double[] xOther = {1.5, 2.5};

            double densityAtMean = dist2D.density(xMean);
            double densityOther = dist2D.density(xOther);

            assertTrue(densityAtMean > densityOther, "La densité doit être maximale à la moyenne (le mode de la gaussienne)");
        }

        @Test
        @DisplayName("Doit calculer la valeur exacte théorique pour la loi normale standard 1D")
        void testStandard1DDensity() {
            // N(0, 1) -> f(0) = 1 / √(2π) ≈ 0.39894228
            Vector mean1D = new ArrayVector(new double[]{0.0});
            Matrix cov1D = new DenseMatrix(new double[][]{{1.0}});
            MultivariateNormalDistribution dist1D = new MultivariateNormalDistribution(mean1D, cov1D);

            double expectedDensityAtZero = 1.0 / Math.sqrt(2 * Math.PI);
            double expectedLogDensityAtZero = Math.log(expectedDensityAtZero);

            assertEquals(expectedLogDensityAtZero, dist1D.logDensity(new double[]{0.0}), EPSILON);
            assertEquals(expectedDensityAtZero, dist1D.density(new double[]{0.0}), EPSILON);
        }

        @Test
        @DisplayName("logDensity et density doivent être parfaitement cohérents (density == exp(logDensity))")
        void testLogDensityConsistency() {
            double[] x = {2.0, 1.0};
            double logDensity = dist2D.logDensity(x);
            double density = dist2D.density(x);

            assertEquals(Math.exp(logDensity), density, EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests d'échantillonnage aléatoire (sample)")
    class SamplingTests {

        @Test
        @DisplayName("Les échantillons générés doivent respecter la moyenne et la covariance théoriques (Loi des grands nombres)")
        void testSampleEmpiricalMoments() {
            int numSamples = 100_000;
            double sumX0 = 0.0, sumX1 = 0.0;
            double sumX0Sq = 0.0, sumX1Sq = 0.0, sumX0X1 = 0.0;

            MultivariateNormalDistribution dist = new MultivariateNormalDistribution(mean2D, cov2D, new Random(12345));

            for (int i = 0; i < numSamples; i++) {
                Vector sample = dist.sample();
                double x0 = sample.getValue(0);
                double x1 = sample.getValue(1);

                sumX0 += x0;
                sumX1 += x1;
                sumX0Sq += x0 * x0;
                sumX1Sq += x1 * x1;
                sumX0X1 += x0 * x1;
            }

            // Moyennes empiriques
            double empiricalMean0 = sumX0 / numSamples;
            double empiricalMean1 = sumX1 / numSamples;

            // Covariances empiriques : Cov(X, Y) = E[XY] - E[X]E[Y]
            double empiricalVar0 = (sumX0Sq / numSamples) - (empiricalMean0 * empiricalMean0);
            double empiricalVar1 = (sumX1Sq / numSamples) - (empiricalMean1 * empiricalMean1);
            double empiricalCov01 = (sumX0X1 / numSamples) - (empiricalMean0 * empiricalMean1);

            // Tolérance empirique à 0.02 près pour 100 000 tirages
            assertEquals(1.0, empiricalMean0, 0.02, "Moyenne μ_0 empirique incorrecte");
            assertEquals(2.0, empiricalMean1, 0.02, "Moyenne μ_1 empirique incorrecte");
            assertEquals(2.0, empiricalVar0, 0.03, "Variance Σ_00 empirique incorrecte");
            assertEquals(1.0, empiricalVar1, 0.03, "Variance Σ_11 empirique incorrecte");
            assertEquals(0.5, empiricalCov01, 0.03, "Covariance Σ_01 empirique incorrecte");
        }
    }

    @Nested
    @DisplayName("Tests de Transformation Linéaire Y = A*X + c")
    class LinearTransformationTests {

        @Test
        @DisplayName("Doit calculer correctement la nouvelle moyenne et la nouvelle covariance")
        void testLinearTransformation() {
            // A = [ 2  0 ]
            //     [ 1  3 ]
            Matrix A = new DenseMatrix(new double[][]{
                    {2.0, 0.0},
                    {1.0, 3.0}
            });

            // c = [ 1, -1 ]^T
            Vector c = new ArrayVector(new double[]{1.0, -1.0});

            MultivariateNormalDistribution transformed = dist2D.linearTransformation(A, c);

            // Nouvelle moyenne : μ_Y = A * μ + c
            // A * [1, 2]^T = [2, 7]^T  ==>  [2, 7]^T + [1, -1]^T = [3, 6]^T
            assertEquals(2, transformed.getDimension());
            assertEquals(3.0, transformed.getMean().getValue(0), EPSILON);
            assertEquals(6.0, transformed.getMean().getValue(1), EPSILON);

            // Nouvelle covariance : Σ_Y = A * Σ * A^T
            // Vérification sur le premier élément de la diagonale Var(Y0) = A_0 * Σ * A_0^T
            // A_0 = [2, 0] => Var(Y0) = 4 * Σ_00 = 4 * 2.0 = 8.0
            assertEquals(8.0, transformed.getCovariance().get(0, 0), EPSILON);
        }

        @Test
        @DisplayName("Lève une exception si les dimensions de la matrice A sont incompatibles")
        void testIncompatibleTransformationMatrix() {
            Matrix invalidA = new DenseMatrix(new double[][]{{1.0, 2.0, 3.0}}); // 1x3 au lieu de ?x2
            Vector c = new ArrayVector(new double[]{0.0});

            assertThrows(
                    IllegalArgumentException.class,
                    () -> dist2D.linearTransformation(invalidA, c)
            );
        }
    }

    @Nested
    @DisplayName("Tests d'Exceptions et de Validation")
    class ValidationExceptionsTests {

        @Test
        @DisplayName("Lève NullPointerException si la moyenne ou la covariance est null")
        void testNullInputs() {
            assertThrows(NullPointerException.class, () -> new MultivariateNormalDistribution(null, cov2D));
            assertThrows(NullPointerException.class, () -> new MultivariateNormalDistribution(mean2D, null));
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la dimension de Σ ne correspond pas à la taille de μ")
        void testDimensionMismatch() {
            Vector mean3D = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateNormalDistribution(mean3D, cov2D)
            );
        }

        @Test
        @DisplayName("Lève IllegalArgumentException lors de logDensity si le vecteur x n'a pas la bonne taille")
        void testInvalidXDimension() {
            double[] invalidX = {1.0, 2.0, 3.0};
            assertThrows(
                    IllegalArgumentException.class,
                    () -> dist2D.logDensity(invalidX)
            );
        }

        @Test
        @DisplayName("Lève IllegalArgumentException si la matrice de covariance n'est pas définie positive")
        void testNonPositiveDefiniteCovariance() {
            // Matrice non définie positive (déterminant négatif/nul)
            Matrix invalidCov = new DenseMatrix(new double[][]{
                    {1.0, 2.0},
                    {2.0, 1.0}
            });

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateNormalDistribution(mean2D, invalidCov)
            );
        }

        @Test
        @DisplayName("Deux instances avec le même seed Random doivent générer exactement les mêmes échantillons")
        void testReproducibilityWithSameSeed() {
            MultivariateNormalDistribution dist1 = new MultivariateNormalDistribution(mean2D, cov2D, new Random(42));
            MultivariateNormalDistribution dist2 = new MultivariateNormalDistribution(mean2D, cov2D, new Random(42));

            Vector s1 = dist1.sample();
            Vector s2 = dist2.sample();

            assertEquals(s1.getValue(0), s2.getValue(0), EPSILON);
            assertEquals(s1.getValue(1), s2.getValue(1), EPSILON);
        }
    }
    @Test
    @DisplayName("Lève une exception si la matrice de covariance n'est pas symétrique")
    void testAsymmetricCovariance() {
        Matrix asymmetricCov = new DenseMatrix(new double[][]{
                {2.0, 1.0},
                {0.0, 1.0} // Non symétrique (0.0 != 1.0)
        });

        assertThrows(
                IllegalArgumentException.class,
                () -> new MultivariateNormalDistribution(mean2D, asymmetricCov)
        );
    }
}