package org.statistics.frequentist.estimator.parametric.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires pour le modèle de régression linéaire simple.
 */
class SimpleLinearRegressionModelTest {

    private static final double EPSILON = 1e-10;
    private SimpleLinearRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new SimpleLinearRegressionModel();
    }

    @Test
    void testIsValidParameterSet_ValidParameters() {
        Vector validParams = new ArrayVector(new double[]{1.5, -2.0, 0.5});
        assertTrue(model.isValidParameterSet(validParams), "Les paramètres valides [1.5, -2.0, 0.5] doivent être acceptés");
    }

    @Test
    void testIsValidParameterSet_InvalidSizeOrNull() {
        assertFalse(model.isValidParameterSet(null), "Un vecteur null doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0})), "Un vecteur de taille 2 doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0, 1.0, 0.5})), "Un vecteur de taille 4 doit être rejeté");
    }

    @Test
    void testIsValidParameterSet_InvalidValues() {
        // Sigma nul ou négatif
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0, 0.0})), "Sigma = 0 doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0, -1.5})), "Sigma < 0 doit être rejeté");

        // Valeurs non finies (NaN, Infinity)
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{Double.NaN, 2.0, 1.0})), "beta_0 = NaN doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, Double.POSITIVE_INFINITY, 1.0})), "beta_1 = Infinity doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0, Double.NaN})), "Sigma = NaN doit être rejeté");
        assertFalse(model.isValidParameterSet(new ArrayVector(new double[]{1.0, 2.0, Double.POSITIVE_INFINITY})), "Sigma = Infinity doit être rejeté");
    }

    @Test
    void testPredict_ValidComputation() {
        // r(x) = 2.0 + 3.0 * x
        Vector params = new ArrayVector(new double[]{2.0, 3.0, 1.0});

        assertEquals(2.0, model.predict(0.0, params), EPSILON, "Prédiction en x=0 doit être égale à beta_0");
        assertEquals(14.0, model.predict(4.0, params), EPSILON, "Prédiction en x=4 doit être 2 + 3*4 = 14");
        assertEquals(-4.0, model.predict(-2.0, params), EPSILON, "Prédiction en x=-2 doit être 2 + 3*(-2) = -4");
    }

    @Test
    void testPredict_ThrowsOnInvalidParams() {
        Vector invalidParams = new ArrayVector(new double[]{2.0, 3.0, -0.5});
        assertThrows(IllegalArgumentException.class, () -> model.predict(1.0, invalidParams));
    }

    @Test
    void testLogLikelihood_ExactPointOnLine() {
        // r(x) = 1.0 + 2.0 * x, sigma = 1.0
        Vector params = new ArrayVector(new double[]{1.0, 2.0, 1.0});
        double x = 3.0;
        double y = 7.0; // Exactement sur la droite (z = 0)

        // ln(pdf) = -ln(1.0) - 0.5 * ln(2 * PI) - 0 = -0.5 * ln(2 * PI)
        double expectedLogL = -0.5 * Math.log(2.0 * Math.PI);
        assertEquals(expectedLogL, model.logLikelihood(y, x, params), EPSILON);
    }

    @Test
    void testLogLikelihood_WithResidualAndNonUnitSigma() {
        // r(x) = 1.0 + 2.0 * x, sigma = 2.0
        Vector params = new ArrayVector(new double[]{1.0, 2.0, 2.0});
        double x = 2.0; // mean = 5.0
        double y = 7.0; // z = (7.0 - 5.0) / 2.0 = 1.0

        // ln(pdf) = -ln(2.0) - 0.5 * ln(2 * PI) - 0.5 * (1.0)^2
        double expectedLogL = -Math.log(2.0) - 0.5 * Math.log(2.0 * Math.PI) - 0.5;
        assertEquals(expectedLogL, model.logLikelihood(y, x, params), EPSILON);
    }

    @Test
    void testLogLikelihood_ThrowsOnInvalidParams() {
        Vector invalidParams = new ArrayVector(new double[]{1.0, 2.0, 0.0});
        assertThrows(IllegalArgumentException.class, () -> model.logLikelihood(5.0, 2.0, invalidParams));
    }
}