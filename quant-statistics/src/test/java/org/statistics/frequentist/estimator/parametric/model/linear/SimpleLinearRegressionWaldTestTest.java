package org.statistics.frequentist.estimator.parametric.model.linear;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;
import org.statistics.frequentist.hypothesis.HypothesisTestResult;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLinearRegressionWaldTestTest {

    private static final double TOLERANCE = 1e-6;

    private final SimpleLinearLeastSquaresEstimator estimator = new SimpleLinearLeastSquaresEstimator();
    private final SimpleLinearRegressionWaldTest waldTest =
            new SimpleLinearRegressionWaldTest(new NormalDistribution(0.0, 1.0));

    @Nested
    @DisplayName("Cas nominaux du test de Wald sur beta_1")
    class NominalCases {

        @Test
        @DisplayName("Rejette H0 : beta_1 = 0 quand la pente est significativement non nulle")
        void shouldRejectNullWhenSlopeIsNonZero() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector y = new ArrayVector(new double[]{2.1, 4.0, 5.9, 8.2, 10.1}); // pente proche de 2

            SimpleLinearRegressionResult fit = estimator.fit(x, y);
            HypothesisTestResult result = waldTest.testBeta1(fit, 0.0);

            double expectedStat = fit.beta1() / fit.beta1StandardError();
            assertTrue(Math.abs(result.testStatistic() - expectedStat) < TOLERANCE);
            assertTrue(result.testStatistic() > 0.0);
            assertTrue(result.pValue() >= 0.0 && result.pValue() < 0.01);
        }

        @Test
        @DisplayName("Produit une p-value élevée lorsque la pente estimée est proche de nullValue")
        void shouldProduceLargePValueWhenSlopeMatchesNull() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector y = new ArrayVector(new double[]{3.0, 3.1, 2.9, 3.05, 2.95}); // pente proche de 0

            SimpleLinearRegressionResult fit = estimator.fit(x, y);
            HypothesisTestResult result = waldTest.testBeta1(fit, 0.0);

            assertTrue(Math.abs(result.testStatistic()) < 1.0);
            assertTrue(result.pValue() > 0.3);
        }

        @Test
        @DisplayName("Teste contre une valeur nulle non zéro (H0 : beta_1 = 2.0)")
        void shouldTestAgainstNonZeroNullValue() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            Vector y = new ArrayVector(new double[]{3.0, 5.0, 7.0, 9.0, 11.0}); // pente exacte = 2.0, bruit nul -> se = 0

            SimpleLinearRegressionResult fit = estimator.fit(x, y);
            HypothesisTestResult result = waldTest.testBeta1(fit, 2.0);

            assertTrue(Math.abs(result.testStatistic()) < TOLERANCE);
            assertTrue(Math.abs(result.pValue() - 1.0) < TOLERANCE);
        }
    }

    @Nested
    @DisplayName("Cas limites et validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Rejette une loi normale nulle au constructeur")
        void shouldRejectNullStandardNormalDistribution() {
            assertThrows(IllegalArgumentException.class, () -> new SimpleLinearRegressionWaldTest(null));
        }

        @Test
        @DisplayName("Retourne une statistique infinie et p-value nulle quand l'erreur standard est nulle et diff != 0")
        void shouldReturnInfiniteStatisticWhenStandardErrorIsZeroAndDiffNonZero() {
            Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});
            Vector y = new ArrayVector(new double[]{3.0, 5.0, 7.0, 9.0}); // beta_1 = 2.0, se = 0.0

            SimpleLinearRegressionResult fit = estimator.fit(x, y);
            HypothesisTestResult result = waldTest.testBeta1(fit, 0.0);

            assertTrue(Double.isInfinite(result.testStatistic()) && result.testStatistic() > 0.0);
            assertTrue(Math.abs(result.pValue()) < TOLERANCE);
        }
    }
}