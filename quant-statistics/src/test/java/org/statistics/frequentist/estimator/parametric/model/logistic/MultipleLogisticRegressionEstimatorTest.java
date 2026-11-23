package org.statistics.frequentist.estimator.parametric.model.logistic;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests unitaires pour {@link MultipleLogisticRegressionEstimator} et {@link MultipleLogisticRegressionResult}.
 */
@DisplayName("MultipleLogisticRegressionEstimator")
class MultipleLogisticRegressionEstimatorTest {

    private static final Logger log = LoggerFactory.getLogger(MultipleLogisticRegressionEstimatorTest.class);
    private static final double EPSILON = 1e-6;

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("Should reject non-positive maxIterations")
        void shouldRejectNonPositiveMaxIterations() {
            log.trace("shouldRejectNonPositiveMaxIterations started");
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(0, 1e-8));
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(-5, 1e-8));
            log.debug("shouldRejectNonPositiveMaxIterations passed");
        }

        @Test
        @DisplayName("Should reject invalid tolerance values")
        void shouldRejectInvalidTolerance() {
            log.trace("shouldRejectInvalidTolerance started");
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(10, 0.0));
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(10, -1e-4));
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(10, Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> new MultipleLogisticRegressionEstimator(10, Double.POSITIVE_INFINITY));
            log.debug("shouldRejectInvalidTolerance passed");
        }
    }

    @Nested
    @DisplayName("Input validation")
    class ValidationTests {

        private final MultipleLogisticRegressionEstimator estimator = new MultipleLogisticRegressionEstimator();

        @Test
        @DisplayName("Should reject null inputs, mismatched sizes, or n <= k")
        void shouldRejectInvalidDimensionsOrNulls() {
            log.trace("shouldRejectInvalidDimensionsOrNulls started");
            Matrix validX = new DenseMatrix(new double[][] {
                    {1.0, 0.5},
                    {1.0, 1.5},
                    {1.0, 2.5}
            });
            Vector validY = new ArrayVector(new double[] {0.0, 0.0, 1.0});

            assertThrows(IllegalArgumentException.class, () -> estimator.fit(null, validY));
            assertThrows(IllegalArgumentException.class, () -> estimator.fit(validX, null));
            // Taille de y incompatible avec le nombre de lignes de X (3 != 2)
            assertThrows(IllegalArgumentException.class,
                    () -> estimator.fit(validX, new ArrayVector(new double[] {0.0, 1.0})));
            // Echantillon sous-déterminé : n = 2 alors que k = 2 (n <= k)
            Matrix underdeterminedX = new DenseMatrix(new double[][] {
                    {1.0, 0.5},
                    {1.0, 1.5}
            });
            assertThrows(IllegalArgumentException.class,
                    () -> estimator.fit(underdeterminedX, new ArrayVector(new double[] {0.0, 1.0})));
            log.debug("shouldRejectInvalidDimensionsOrNulls passed");
        }

        @Test
        @DisplayName("Should reject non-binary target values or non-finite design entries")
        void shouldRejectInvalidValues() {
            log.trace("shouldRejectInvalidValues started");
            Matrix validX = new DenseMatrix(new double[][] {
                    {1.0, -1.0},
                    {1.0,  0.0},
                    {1.0,  1.0},
                    {1.0,  2.0}
            });
            assertThrows(IllegalArgumentException.class,
                    () -> estimator.fit(validX, new ArrayVector(new double[] {0.0, 0.5, 1.0, 1.0})));

            Matrix nanX = new DenseMatrix(new double[][] {
                    {1.0, -1.0},
                    {1.0,  Double.NaN},
                    {1.0,  1.0},
                    {1.0,  2.0}
            });
            Vector validY = new ArrayVector(new double[] {0.0, 0.0, 1.0, 1.0});
            assertThrows(IllegalArgumentException.class, () -> estimator.fit(nanX, validY));
            log.debug("shouldRejectInvalidValues passed");
        }
    }

    @Nested
    @DisplayName("IRLS estimation and inference")
    class EstimationTests {

        @Test
        @DisplayName("Should converge on overlapping dataset and return coherent statistics")
        void shouldFitLogisticModelOnOverlappingData() {
            log.trace("shouldFitLogisticModelOnOverlappingData started");
            // Chevauchement des valeurs de x entre y=0 et y=1 pour garantir un MLE fini
            Matrix x = new DenseMatrix(new double[][] {
                    {1.0, -2.0},
                    {1.0, -0.5},
                    {1.0,  0.2},
                    {1.0, -0.2},
                    {1.0,  0.5},
                    {1.0,  2.0}
            });
            Vector y = new ArrayVector(new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0});

            MultipleLogisticRegressionEstimator estimator =
                    new MultipleLogisticRegressionEstimator(100, 1e-10);
            MultipleLogisticRegressionResult result = estimator.fit(x, y);
            Vector estimatedBeta = estimator.estimate(x, y);

            assertTrue(result.converged());
            assertTrue(result.iterations() > 0 && result.iterations() <= 100);
            assertTrue(result.coefficients().size() == 2);
            assertTrue(estimatedBeta.size() == 2);
            assertTrue(Math.abs(result.coefficients().getValue(0) - estimatedBeta.getValue(0)) < 1e-12);
            assertTrue(Math.abs(result.coefficients().getValue(1) - estimatedBeta.getValue(1)) < 1e-12);
            assertTrue(Math.abs(result.coefficients().getValue(0)) < 1e-6);
            assertTrue(result.coefficients().getValue(1) > 0.0);

            assertTrue(result.fittedProbabilities().size() == 6);
            for (int i = 0; i < 6; i++) {
                double p = result.fittedProbabilities().getValue(i);
                assertTrue(p > 0.0 && p < 1.0);
            }
            assertTrue(result.logLikelihood() < 0.0);
            assertTrue(Double.isFinite(result.logLikelihood()));

            double se0 = result.standardError(0);
            double se1 = result.standardError(1);
            assertTrue(se0 > 0.0 && Double.isFinite(se0));
            assertTrue(se1 > 0.0 && Double.isFinite(se1));
            assertTrue(Math.abs(se0 - Math.sqrt(result.asymptoticCovariance().get(0, 0))) < EPSILON);
            assertTrue(Math.abs(se1 - Math.sqrt(result.asymptoticCovariance().get(1, 1))) < EPSILON);

            log.debug("shouldFitLogisticModelOnOverlappingData passed with beta0={}, beta1={}, logLik={}",
                    result.coefficients().getValue(0),
                    result.coefficients().getValue(1),
                    result.logLikelihood());
        }

        @Test
        @DisplayName("Should report converged=false when maxIterations is too low on overlapping data")
        void shouldReportNonConvergenceWhenIterationBudgetExceeded() {
            log.trace("shouldReportNonConvergenceWhenIterationBudgetExceeded started");
            Matrix x = new DenseMatrix(new double[][] {
                    {1.0, -2.0},
                    {1.0, -0.5},
                    {1.0,  0.2},
                    {1.0, -0.2},
                    {1.0,  0.5},
                    {1.0,  2.0}
            });
            Vector y = new ArrayVector(new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0});

            MultipleLogisticRegressionEstimator estimator =
                    new MultipleLogisticRegressionEstimator(1, 1e-12);
            MultipleLogisticRegressionResult result = estimator.fit(x, y);

            assertTrue(!result.converged());
            assertTrue(result.iterations() == 1);
            log.debug("shouldReportNonConvergenceWhenIterationBudgetExceeded passed");
        }

        @Test
        @DisplayName("Should reject out-of-bounds index in standardError")
        void shouldRejectInvalidStandardErrorIndex() {
            log.trace("shouldRejectInvalidStandardErrorIndex started");
            Matrix x = new DenseMatrix(new double[][] {
                    {1.0, -2.0},
                    {1.0, -0.5},
                    {1.0,  0.2},
                    {1.0, -0.2},
                    {1.0,  0.5},
                    {1.0,  2.0}
            });
            Vector y = new ArrayVector(new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0});
            MultipleLogisticRegressionResult result =
                    new MultipleLogisticRegressionEstimator().fit(x, y);

            assertTrue(result.converged());
            assertThrows(IllegalArgumentException.class, () -> result.standardError(-1));
            assertThrows(IllegalArgumentException.class, () -> result.standardError(2));
            log.debug("shouldRejectInvalidStandardErrorIndex passed");
        }
    }
}