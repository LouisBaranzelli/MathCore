package org.statistics.frequentist.estimator.parametric.model.linear;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipleLinearLeastSquaresEstimatorTest {

    private static final double TOLERANCE = 1e-8;
    private final MultipleLinearLeastSquaresEstimator estimator = new MultipleLinearLeastSquaresEstimator();

    @Nested
    @DisplayName("Cas nominaux de régression multiple")
    class NominalCases {

        @Test
        @DisplayName("Retrouve exactement les coefficients sur un plan linéaire sans bruit avec intercept")
        void shouldRecoverExactCoefficientsOnNoiseFreePlane() {
            // y = 2.0 + 3.0 * x1 - 1.5 * x2
            Matrix x = new DenseMatrix(new double[][]{
                    {1.0, 1.0, 2.0},
                    {1.0, 2.0, 1.0},
                    {1.0, 3.0, 4.0},
                    {1.0, 4.0, 3.0},
                    {1.0, 5.0, 2.0}
            });
            Vector y = new ArrayVector(new double[]{
                    2.0 + 3.0 * 1.0 - 1.5 * 2.0,
                    2.0 + 3.0 * 2.0 - 1.5 * 1.0,
                    2.0 + 3.0 * 3.0 - 1.5 * 4.0,
                    2.0 + 3.0 * 4.0 - 1.5 * 3.0,
                    2.0 + 3.0 * 5.0 - 1.5 * 2.0
            });

            MultipleLinearRegressionResult result = estimator.fit(x, y);

            assertTrue(Math.abs(result.coefficients().getValue(0) - 2.0) < TOLERANCE);
            assertTrue(Math.abs(result.coefficients().getValue(1) - 3.0) < TOLERANCE);
            assertTrue(Math.abs(result.coefficients().getValue(2) + 1.5) < TOLERANCE);
            assertTrue(result.sigma() < TOLERANCE);
            assertTrue(Math.abs(result.rSquared() - 1.0) < TOLERANCE);
        }

        @Test
        @DisplayName("Produit une matrice de covariance symétrique et des erreurs standard positives")
        void shouldProduceSymmetricCovarianceAndPositiveStandardErrors() {
            Matrix x = new DenseMatrix(new double[][]{
                    {1.0, 1.0, 0.5},
                    {1.0, 2.0, 1.5},
                    {1.0, 3.0, 1.0},
                    {1.0, 4.0, 2.5},
                    {1.0, 5.0, 2.0},
                    {1.0, 6.0, 3.5}
            });
            Vector y = new ArrayVector(new double[]{2.1, 4.2, 4.8, 7.3, 7.9, 10.4});

            MultipleLinearRegressionResult result = estimator.fit(x, y);

            assertTrue(result.sigma() > 0.0);
            assertTrue(result.rSquared() > 0.0 && result.rSquared() <= 1.0);
            for (int i = 0; i < result.covariateCount(); i++) {
                assertTrue(result.standardError(i) > 0.0);
                for (int j = 0; j < result.covariateCount(); j++) {
                    double a = result.coefficientCovariance().get(i, j);
                    double b = result.coefficientCovariance().get(j, i);
                    assertTrue(Math.abs(a - b) < TOLERANCE);
                }
            }
        }
    }

    @Nested
    @DisplayName("Validation des dimensions et cas dégénérés")
    class ValidationAndEdgeCases {

        @Test
        @DisplayName("Rejette un échantillon avec n <= k degrés de liberté insuffisants")
        void shouldRejectWhenSampleSizeNotGreaterThanCovariateCount() {
            Matrix x = new DenseMatrix(new double[][]{
                    {1.0, 2.0},
                    {1.0, 3.0}
            });
            Vector y = new ArrayVector(new double[]{4.0, 6.0});

            assertThrows(IllegalArgumentException.class, () -> estimator.fit(x, y));
        }

        @Test
        @DisplayName("Rejette une matrice X colinéaire non inversible")
        void shouldRejectSingularDesignMatrix() {
            Matrix x = new DenseMatrix(new double[][]{
                    {1.0, 2.0, 4.0},
                    {1.0, 3.0, 6.0},
                    {1.0, 4.0, 8.0},
                    {1.0, 5.0, 10.0}
            });
            Vector y = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});

            assertThrows(RuntimeException.class, () -> estimator.fit(x, y));
        }
    }
}