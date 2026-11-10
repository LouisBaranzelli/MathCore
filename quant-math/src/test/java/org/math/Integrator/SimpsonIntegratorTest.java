package org.math.Integrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class SimpsonIntegratorTest {

    private SimpsonIntegrator integrator;
    private static final double EPSILON = 1e-6;

    @BeforeEach
    void setUp() {
        integrator = new SimpsonIntegrator(1000); // 1000 intervalles pour une haute précision
    }

    @Test
    @DisplayName("Should return zero when integration bounds are identical")
    void testIdenticalBounds() {
        ToDoubleFunction<Double> f = x -> x * x;
        double result = integrator.integrate(f, 5.0, 5.0);
        assertEquals(0.0, result, 1e-12);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when bounds are invalid (a > b)")
    void testInvalidBounds() {
        ToDoubleFunction<Double> f = x -> x;
        assertThrows(IllegalArgumentException.class, () -> integrator.integrate(f, 2.0, 1.0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for NaN or Infinite bounds")
    void testInfiniteOrNaNBounds() {
        ToDoubleFunction<Double> f = x -> x;
        assertThrows(IllegalArgumentException.class, () -> integrator.integrate(f, 0.0, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> integrator.integrate(f, Double.NaN, 1.0));
    }

    @Test
    @DisplayName("Should adjust odd number of intervals to the next even number")
    void testOddIntervalsAdjustment() {
        SimpsonIntegrator oddIntegrator = new SimpsonIntegrator(101);
        // On vérifie simplement que l'intégration s'exécute sans erreur grâce à l'ajustement automatique
        ToDoubleFunction<Double> f = x -> x;
        double result = oddIntegrator.integrate(f, 0.0, 1.0);
        assertEquals(0.5, result, 1e-2);
    }

    @Test
    @DisplayName("Integral of f(x) = x^2 from 0 to 3 should equal 9.0 (Exact analytical = 9)")
    void testPolynomialFunction() {
        // Intégrale de x^2 dx de 0 à 3 = [x^3 / 3] = 27 / 3 = 9.0
        ToDoubleFunction<Double> f = x -> x * x;
        double result = integrator.integrate(f, 0.0, 3.0);
        assertEquals(9.0, result, EPSILON);
    }

    @Test
    @DisplayName("Integral of f(x) = sin(x) from 0 to Pi should equal 2.0")
    void testTrigonometricFunction() {
        // Intégrale de sin(x) dx de 0 à Pi = [-cos(x)] = -cos(Pi) - (-cos(0)) = 1 - (-1) = 2.0
        ToDoubleFunction<Double> f = Math::sin;
        double result = integrator.integrate(f, 0.0, Math.PI);
        assertEquals(2.0, result, EPSILON);
    }

    @Test
    @DisplayName("Integral of f(x) = e^x from 0 to 1 should equal e - 1 (~1.7182818)")
    void testExponentialFunction() {
        // Intégrale de e^x dx de 0 à 1 = e^1 - e^0 = e - 1
        ToDoubleFunction<Double> f = Math::exp;
        double expected = Math.E - 1.0;
        double result = integrator.integrate(f, 0.0, 1.0);
        assertEquals(expected, result, EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0, 0.3333333333", // Intégrale de x^2 de 0 à 1 = 1/3
            "-1.0, 1.0, 0.6666666666", // Intégrale de x^2 de -1 à 1 = 2/3
            "1.0, 2.0, 2.3333333333"  // Intégrale de x^2 de 1 à 2 = [x^3/3] = (8-1)/3 = 7/3
    })
    @DisplayName("Parameterized tests for f(x) = x^2 over various intervals")
    void testParameterizedPolynomial(double a, double b, double expected) {
        ToDoubleFunction<Double> f = x -> x * x;
        double result = integrator.integrate(f, a, b);
        assertEquals(expected, result, 1e-5);
    }
}