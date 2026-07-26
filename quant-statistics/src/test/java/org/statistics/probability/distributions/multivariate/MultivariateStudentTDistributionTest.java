package org.statistics.probability.distributions.multivariate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.matrix.DenseMatrix; // Adapte l'import selon ton implémentation de Matrix
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests pour MultivariateStudentTDistribution avec JUnit Jupiter")
class MultivariateStudentTDistributionTest {

    private Vector mean2D;
    private Matrix scaleMatrix2D;
    private static final double EPSILON = 1e-9;

    @BeforeEach
    void setUp() {
        // Vecteur μ = [1.0, 2.0]
        mean2D = new ArrayVector(new double[]{1.0, 2.0});

        // Matrice Σ d'échelle 2x2 symétrique et définie positive
        // [ 2.0  0.5 ]
        // [ 0.5  1.0 ]
        double[][] data = {
                {2.0, 0.5},
                {0.5, 1.0}
        };
        scaleMatrix2D = new DenseMatrix(data);
    }

    @Nested
    @DisplayName("Validation de l'instanciation et du constructeur")
    class ConstructorTests {

        @Test
        @DisplayName("Devrait créer une distribution valide avec des paramètres corrects")
        void shouldCreateValidDistribution() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D);

            assertEquals(4.0, dist.getDegreesOfFreedom(), EPSILON);
            assertEquals(2, dist.getDimension());
            assertEquals(scaleMatrix2D, dist.getScaleMatrix());
        }

        @Test
        @DisplayName("Devrait lever une exception si nu <= 0")
        void shouldThrowWhenDegreesOfFreedomInvalid() {
            IllegalArgumentException ex1 = assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateStudentTDistribution(0.0, mean2D, scaleMatrix2D)
            );
            assertTrue(ex1.getMessage().contains("strictly positifs"));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateStudentTDistribution(-2.5, mean2D, scaleMatrix2D)
            );
        }

        @Test
        @DisplayName("Devrait lever une exception si mean ou scaleMatrix est null")
        void shouldThrowWhenNullArguments() {
            NullPointerException exMean = assertThrows(
                    NullPointerException.class,
                    () -> new MultivariateStudentTDistribution(4.0, null, scaleMatrix2D)
            );
            assertTrue(exMean.getMessage().contains("vecteur de moyenne"));

            NullPointerException exMatrix = assertThrows(
                    NullPointerException.class,
                    () -> new MultivariateStudentTDistribution(4.0, mean2D, null)
            );
            assertTrue(exMatrix.getMessage().contains("matrice d'échelle"));
        }

        @Test
        @DisplayName("Devrait lever une exception si les dimensions ne correspondent pas")
        void shouldThrowWhenDimensionMismatch() {
            Vector mean3D = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateStudentTDistribution(4.0, mean3D, scaleMatrix2D)
            );
            assertTrue(ex.getMessage().contains("Incohérence de dimension"));
        }

        @Test
        @DisplayName("Devrait lever une exception si la matrice d'échelle n'est pas symétrique")
        void shouldThrowWhenScaleMatrixIsNotSymmetric() {
            double[][] nonSymmetricData = {
                    {2.0, 0.8},
                    {0.1, 1.0}
            };
            Matrix nonSymmetricMatrix = new DenseMatrix(nonSymmetricData);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new MultivariateStudentTDistribution(4.0, mean2D, nonSymmetricMatrix)
            );
            assertTrue(ex.getMessage().contains("pas symétrique"));
        }
    }

    @Nested
    @DisplayName("Tests des propriétés statistiques (Moyenne et Covariance)")
    class StatisticalPropertiesTests {

        @Test
        @DisplayName("Devrait retourner la moyenne lorsque nu > 1")
        void shouldReturnMeanWhenNuGreaterThanOne() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(1.5, mean2D, scaleMatrix2D);
            assertEquals(mean2D, dist.getMean());
        }

        @Test
        @DisplayName("Devrait lever une exception pour getMean() lorsque nu <= 1")
        void shouldThrowForMeanWhenNuLessOrEqualToOne() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(1.0, mean2D, scaleMatrix2D);

            IllegalStateException ex = assertThrows(IllegalStateException.class, dist::getMean);
            assertTrue(ex.getMessage().contains("indéfinie pour nu <= 1"));
        }

        @Test
        @DisplayName("Devrait calculer correctement la matrice de covariance lorsque nu > 2")
        void shouldCalculateCovarianceWhenNuGreaterThanTwo() {
            double nu = 4.0;
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(nu, mean2D, scaleMatrix2D);

            // Facteur = nu / (nu - 2) = 4 / 2 = 2.0
            Matrix cov = dist.getCovariance();

            assertEquals(4.0, cov.get(0, 0), EPSILON); // 2.0 * 2.0
            assertEquals(1.0, cov.get(0, 1), EPSILON); // 0.5 * 2.0
            assertEquals(2.0, cov.get(1, 1), EPSILON); // 1.0 * 2.0
        }

        @Test
        @DisplayName("Devrait lever une exception pour getCovariance() lorsque nu <= 2")
        void shouldThrowForCovarianceWhenNuLessOrEqualToTwo() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(2.0, mean2D, scaleMatrix2D);

            IllegalStateException ex = assertThrows(IllegalStateException.class, dist::getCovariance);
            assertTrue(ex.getMessage().contains("infinie/indéfinie pour nu <= 2"));
        }
    }

    @Nested
    @DisplayName("Tests de calcul de Densité et Log-Densité")
    class DensityTests {

        @Test
        @DisplayName("La densité au point moyen x = μ doit être valide et cohérente")
        void shouldCalculateDensityAtMean() {
            double nu = 5.0;
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(nu, mean2D, scaleMatrix2D);

            double[] xAtMean = new double[]{1.0, 2.0};
            Vector vectorAtMean = new ArrayVector(xAtMean);

            double logDensity = dist.logDensity(xAtMean);
            double density = dist.density(vectorAtMean);

            assertEquals(Math.exp(logDensity), density, EPSILON);
            assertTrue(density > 0.0);
        }

        @Test
        @DisplayName("Les méthodes Vector et double[] doivent produire des résultats identiques")
        void shouldReturnSameDensityForVectorAndArray() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D);
            double[] xArray = new double[]{1.5, 2.5};
            Vector xVector = new ArrayVector(xArray);

            assertEquals(dist.logDensity(xArray), dist.logDensity(xVector), EPSILON);
            assertEquals(dist.density(xArray), dist.density(xVector), EPSILON);
        }

        @Test
        @DisplayName("Devrait lever une exception si le vecteur passé à la densité a une mauvaise dimension")
        void shouldThrowOnDimensionMismatchForDensity() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D);
            double[] invalidX = new double[]{1.0, 2.0, 3.0};

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> dist.logDensity(invalidX)
            );
            assertTrue(ex.getMessage().contains("Dimension du vecteur x incorrecte"));
        }
    }

    @Nested
    @DisplayName("Tests de l'échantillonnage (sample)")
    class SamplingTests {

        @Test
        @DisplayName("Devrait générer un échantillon de la bonne dimension")
        void shouldSampleVectorOfCorrectDimension() {
            MultivariateStudentTDistribution dist = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D);
            Vector sample = dist.sample();

            assertNotNull(sample);
            assertEquals(2, sample.size());
        }

        @Test
        @DisplayName("L'échantillonnage doit être déterministe si la même graine (Random) est utilisée")
        void shouldBeReproducibleWithSameSeed() {
            MultivariateStudentTDistribution dist1 = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D, new Random(42));
            MultivariateStudentTDistribution dist2 = new MultivariateStudentTDistribution(4.0, mean2D, scaleMatrix2D, new Random(42));

            Vector sample1 = dist1.sample();
            Vector sample2 = dist2.sample();

            assertEquals(sample1.getValue(0), sample2.getValue(0), EPSILON);
            assertEquals(sample1.getValue(1), sample2.getValue(1), EPSILON);
        }
    }
}