package org.statistics.frequentist.estimator.parametric.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.frequentist.estimator.parametric.model.linear.SimpleLinearLeastSquaresEstimator;
import org.statistics.frequentist.estimator.parametric.model.linear.SimpleLinearRegressionResult;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLinearLeastSquaresEstimatorTest {

    private static final double TOLERANCE = 1e-9;
    private final SimpleLinearLeastSquaresEstimator estimator = new SimpleLinearLeastSquaresEstimator();

    @Nested
    @DisplayName("Ajustement OLS nominal")
    class NominalFit {

        @Test
        @DisplayName("Estime exactement les paramètres sur une relation linéaire déterministe")
        void shouldEstimateExactParametersOnDeterministicLine() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});
            Vector y = new ArrayVector(new double[]{3.0, 5.0, 7.0, 9.0}); // y = 1 + 2x

            SimpleLinearRegressionResult result = estimator.fit(x, y);

            assertTrue(Math.abs(result.beta0() - 1.0) < TOLERANCE);
            assertTrue(Math.abs(result.beta1() - 2.0) < TOLERANCE);
            assertTrue(Math.abs(result.sigma()) < TOLERANCE);
            assertTrue(Math.abs(result.rSquared() - 1.0) < TOLERANCE);
        }

        @Test
        @DisplayName("Calcule la matrice de covariance et les résidus conformément à la théorie")
        void shouldComputeCovarianceMatrixAndZeroSumResiduals() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});
            Vector y = new ArrayVector(new double[]{2.0, 3.0, 5.0, 4.0});

            SimpleLinearRegressionResult result = estimator.fit(x, y);

            // Somme des résidus nulle en présence d'une constante
            double residualSum = 0.0;
            for (int i = 0; i < result.residuals().size(); i++) {
                residualSum += result.residuals().getValue(i);
            }
            assertTrue(Math.abs(residualSum) < TOLERANCE);

            // Symétrie de la matrice de covariance 2x2
            double cov01 = result.coefficientCovariance().get(0, 1);
            double cov10 = result.coefficientCovariance().get(1, 0);
            assertTrue(Math.abs(cov01 - cov10) < TOLERANCE);
            assertTrue(result.beta0StandardError() > 0.0);
            assertTrue(result.beta1StandardError() > 0.0);
        }
    }

    @Nested
    @DisplayName("Validation des entrées et cas dégénérés")
    class ValidationAndEdgeCases {

        @Test
        @DisplayName("Rejette un prédicteur de variance nulle")
        void shouldRejectZeroVariancePredictor() {
            Vector x = new ArrayVector(new double[]{2.0, 2.0, 2.0});
            Vector y = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            assertThrows(IllegalArgumentException.class, () -> estimator.fit(x, y));
        }

        @Test
        @DisplayName("Retourne NaN pour R^2 lorsque Y est constant")
        void shouldReturnNaNForRSquaredWhenYIsConstant() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0});
            Vector y = new ArrayVector(new double[]{5.0, 5.0, 5.0});

            SimpleLinearRegressionResult result = estimator.fit(x, y);

            assertTrue(Double.isNaN(result.rSquared()));
            assertTrue(Math.abs(result.beta1()) < TOLERANCE);
        }
    }
}