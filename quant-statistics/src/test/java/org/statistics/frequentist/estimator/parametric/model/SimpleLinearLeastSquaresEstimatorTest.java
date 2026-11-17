package org.statistics.frequentist.estimator.parametric.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires pour l'estimateur OLS et son objet de résultat d'inférence.
 */
class SimpleLinearLeastSquaresEstimatorTest {

    private static final double EPSILON = 1e-10;
    private SimpleLinearLeastSquaresEstimator estimator;

    @BeforeEach
    void setUp() {
        estimator = new SimpleLinearLeastSquaresEstimator();
    }

    @Test
    void testEstimate_ExactLinearRelationship() {
        Vector x = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});
        Vector y = new ArrayVector(new double[]{5.0, 8.0, 11.0, 14.0});

        SimpleLinearRegressionResult result = estimator.fit(x, y);

        assertEquals(2.0, result.getBeta0(), EPSILON);
        assertEquals(3.0, result.getBeta1(), EPSILON);
        assertEquals(0.0, result.getSigma(), EPSILON);
        assertEquals(0.0, result.getBeta0StandardError(), EPSILON);
        assertEquals(0.0, result.getBeta1StandardError(), EPSILON);
        assertEquals(1.0, result.getRSquared(), EPSILON);

        for (int i = 0; i < x.size(); i++) {
            assertEquals(0.0, result.getResiduals().getValue(i), EPSILON);
        }
    }

    @Test
    void testFit_WithNoiseStandardErrorsAndResiduals() {
        // Points symétriques autour de y = 1.0 + 2.0 * x
        // x = [1, 1, 3, 3] -> meanX = 2.0, S_xx = 4.0, sum(x^2) = 20.0
        // y = [2, 4, 6, 8] -> meanY = 5.0, S_yy = 20.0
        Vector x = new ArrayVector(new double[]{1.0, 1.0, 3.0, 3.0});
        Vector y = new ArrayVector(new double[]{2.0, 4.0, 6.0, 8.0});

        SimpleLinearRegressionResult result = estimator.fit(x, y);

        assertEquals(1.0, result.getBeta0(), EPSILON);
        assertEquals(2.0, result.getBeta1(), EPSILON);

        // RSS = 4.0 -> sigma = sqrt(4.0 / 2) = sqrt(2)
        double expectedSigma = Math.sqrt(2.0);
        assertEquals(expectedSigma, result.getSigma(), EPSILON);

        // se(beta_1) = sigma / sqrt(S_xx) = sqrt(2) / sqrt(4) = sqrt(2) / 2
        double expectedSeBeta1 = expectedSigma / 2.0;
        assertEquals(expectedSeBeta1, result.getBeta1StandardError(), EPSILON);

        // se(beta_0) = se(beta_1) * sqrt(sum(x^2) / n) = (sqrt(2)/2) * sqrt(20 / 4) = sqrt(10) / 2
        double expectedSeBeta0 = expectedSeBeta1 * Math.sqrt(5.0);
        assertEquals(expectedSeBeta0, result.getBeta0StandardError(), EPSILON);

        // R^2 = 1 - (RSS / S_yy) = 1 - (4.0 / 20.0) = 0.8
        assertEquals(0.8, result.getRSquared(), EPSILON);

        // Statistique t pour beta_1 : 2.0 / (sqrt(2)/2) = 2 * sqrt(2)
        assertEquals(2.0 / expectedSeBeta1, result.getBeta1TStatistic(), EPSILON);

        // Vérification des résidus : [-1, +1, -1, +1]
        Vector residuals = result.getResiduals();
        assertEquals(-1.0, residuals.getValue(0), EPSILON);
        assertEquals(1.0, residuals.getValue(1), EPSILON);
        assertEquals(-1.0, residuals.getValue(2), EPSILON);
        assertEquals(1.0, residuals.getValue(3), EPSILON);

        // Intégration avec le modèle passif
        SimpleLinearRegressionModel model = new SimpleLinearRegressionModel();
        assertTrue(model.isValidParameterSet(result.getParameters()));
        assertEquals(5.0, model.predict(2.0, result.getParameters()), EPSILON);
    }

    @Test
    void testEstimate_InvalidInputs() {
        Vector x = new ArrayVector(new double[]{1.0, 2.0});
        Vector y = new ArrayVector(new double[]{3.0, 4.0});

        assertThrows(IllegalArgumentException.class, () -> estimator.fit(x, y));

        Vector x3 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(x3, y));

        Vector constantX = new ArrayVector(new double[]{2.0, 2.0, 2.0});
        Vector y3 = new ArrayVector(new double[]{1.0, 2.0, 3.0});
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(constantX, y3));
    }
}