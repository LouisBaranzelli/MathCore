package org.math.Integrator.multivariate;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveProductRuleIntegratorTest {

    private MultivariateNumericalIntegrator integrator;

    @BeforeEach
    void setUp() {
        // Initialisation de l'intégrateur avec 20 subdivisions par dimension
        integrator = new RecursiveProductRuleIntegrator(20);
    }

    @Test
    void testIntegrateConstantFunction2D() {
        // Intégration de f(x, y) = 2.0 sur le domaine [0, 2] x [0, 3]
        // Résultat analytique attendu : Volume = 2.0 * (2 - 0) * (3 - 0) = 12.0
        ToDoubleFunction<Vector> constantFunction = theta -> 2.0;

        Vector a = new ArrayVector(new double[]{0.0, 0.0});
        Vector b = new ArrayVector(new double[]{2.0, 3.0});

        double result = integrator.integrate(constantFunction, a, b);

        assertEquals(12.0, result, 1e-4, "L'intégrale d'une fonction constante sur un rectangle 2D doit correspondre au volume.");
    }

    @Test
    void testIntegratePolynomialFunction2D() {
        // Intégration de f(x, y) = x * y sur le domaine [1, 2] x [1, 3]
        // Analytique : ∫_1^2 x dx * ∫_1^3 y dy = [x^2/2]_1^2 * [y^2/2]_1^3
        // = (4/2 - 1/2) * (9/2 - 1/2) = (1.5) * (4.0) = 6.0
        ToDoubleFunction<Vector> polynomialFunction = theta -> theta.getValue(0) * theta.getValue(1);

        Vector a = new ArrayVector(new double[]{1.0, 1.0});
        Vector b = new ArrayVector(new double[]{2.0, 3.0});

        double result = integrator.integrate(polynomialFunction, a, b);

        assertEquals(6.0, result, 1e-3, "L'intégrale numérique de x*y sur le domaine spécifié doit être proche de 6.0.");
    }

    @Test
    void testInvalidStepsPerDimension() {
        assertThrows(IllegalArgumentException.class, () ->
                        new RecursiveProductRuleIntegrator(0),
                "Doit lever une exception si le nombre de pas par dimension est inférieur à 1."
        );
    }

    @Test
    void testValidateBoundsDimensionMismatch() {
        Vector a = new ArrayVector(new double[]{0.0, 0.0}); // Dimension 2
        Vector b = new ArrayVector(new double[]{1.0});       // Dimension 1

        ToDoubleFunction<Vector> dummyFunc = theta -> 1.0;

        assertThrows(IllegalArgumentException.class, () ->
                        integrator.integrate(dummyFunc, a, b),
                "Doit lever une exception si les dimensions des vecteurs de bornes a et b diffèrent."
        );
    }

    @Test
    void testValidateBoundsLowerGreaterThanUpper() {
        Vector a = new ArrayVector(new double[]{2.0, 0.0}); // a[0] = 2.0
        Vector b = new ArrayVector(new double[]{1.0, 3.0}); // b[0] = 1.0 (invalide car a > b)

        ToDoubleFunction<Vector> dummyFunc = theta -> 1.0;

        assertThrows(IllegalArgumentException.class, () ->
                        integrator.integrate(dummyFunc, a, b),
                "Doit lever une exception si une borne inférieure est supérieure à sa borne supérieure."
        );
    }
}