package org.math.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.function.MultivariateFunction;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultivariateFunctionTest {

    private static final double EPSILON = 1e-4;

    @Test
    @DisplayName("Devrait calculer le gradient d'une fonction linéaire f(x, y) = 2x + 5y")
    void shouldComputeGradientForLinearFunction() {
        // f(x, y) = 2x + 5y  =>  grad(f) = [2, 5]
        MultivariateFunction f = point -> 2 * point.getValue(0) + 5 * point.getValue(1);

        Vector x = new ArrayVector(new double[]{3.0, -1.0});
        Vector grad = f.gradient(x);

        assertEquals(2.0, grad.getValue(0), EPSILON);
        assertEquals(5.0, grad.getValue(1), EPSILON);
    }

    @Test
    @DisplayName("Devrait calculer le gradient d'une fonction quadratique f(x, y) = x^2 + 3xy + y^2")
    void shouldComputeGradientForQuadraticFunction() {
        // f(x, y) = x^2 + 3xy + y^2
        // df/dx = 2x + 3y
        // df/dy = 3x + 2y
        MultivariateFunction f = point -> {
            double x = point.getValue(0);
            double y = point.getValue(1);
            return x * x + 3 * x * y + y * y;
        };

        Vector point = new ArrayVector(new double[]{2.0, 4.0});

        // En (2, 4) :
        // df/dx = 2(2) + 3(4) = 16
        // df/dy = 3(2) + 2(4) = 14
        Vector grad = f.gradient(point);

        assertEquals(16.0, grad.getValue(0), EPSILON);
        assertEquals(14.0, grad.getValue(1), EPSILON);
    }

    @Test
    @DisplayName("Devrait prendre en compte un pas customisé (stepSize)")
    void shouldRespectCustomStepSize() {
        MultivariateFunction f = point -> Math.pow(point.getValue(0), 3); // f(x) = x^3 => f'(x) = 3x^2
        Vector point = new ArrayVector(new double[]{2.0});

        // f'(2) = 12
        Vector grad = f.gradient(point, 1e-4);

        assertEquals(12.0, grad.getValue(0), 1e-3);
    }
}