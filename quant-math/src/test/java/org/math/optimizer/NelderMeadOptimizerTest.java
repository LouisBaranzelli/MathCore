package org.math.optimizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.function.MultivariateFunction;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NelderMeadOptimizerTest {

    private static final double EPSILON = 1e-3;

    @Test
    @DisplayName("Devrait minimiser une fonction parabolique simple en 2D f(x, y) = x^2 + y^2")
    void shouldMinimizeSimpleParaboloid() {
        MultivariateFunction paraboloid = v -> {
            double x = v.getValue(0);
            double y = v.getValue(1);
            return x * x + y * y;
        };

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-6, 200);
        Vector initialGuess = new ArrayVector(new double[]{5.0, -4.0});

        OptimizationResult result = optimizer.optimize(paraboloid, initialGuess);

        assertTrue(result.terminationReason().isSuccess(), "L'optimisation aurait dû converger");
        assertEquals(TerminationCriterion.CONVERGED_VALUE_TOL, result.terminationReason());
        assertEquals(0.0, result.minCost(), EPSILON);
        assertEquals(0.0, result.point().getValue(0), EPSILON);
        assertEquals(0.0, result.point().getValue(1), EPSILON);
        assertTrue(result.iterations() < 200, "Le nombre d'itérations doit être inférieur au max");
    }

    @Test
    @DisplayName("Devrait minimiser la fonction de Rosenbrock f(x, y) = (1-x)^2 + 100*(y-x^2)^2")
    void shouldMinimizeRosenbrockFunction() {
        MultivariateFunction rosenbrock = v -> {
            double x = v.getValue(0);
            double y = v.getValue(1);
            return Math.pow(1 - x, 2) + 100 * Math.pow(y - x * x, 2);
        };

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-7, 1000);
        Vector initialGuess = new ArrayVector(new double[]{-1.2, 1.0});

        OptimizationResult result = optimizer.optimize(rosenbrock, initialGuess);

        assertTrue(result.converged(), "L'optimisation aurait dû converger");
        assertEquals(TerminationCriterion.CONVERGED_VALUE_TOL, result.terminationReason());
        assertEquals(0.0, result.minCost(), EPSILON);
        assertEquals(1.0, result.point().getValue(0), EPSILON);
        assertEquals(1.0, result.point().getValue(1), EPSILON);
    }

    @Test
    @DisplayName("Devrait s'arrêter avec MAX_ITERATIONS_REACHED si le budget d'itérations est trop faible")
    void shouldReturnMaxIterationsReachedWhenBudgetIsTooLow() {
        MultivariateFunction paraboloid = v -> v.getValue(0) * v.getValue(0) + v.getValue(1) * v.getValue(1);

        int maxIterations = 2;
        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-8, maxIterations);
        Vector initialGuess = new ArrayVector(new double[]{10.0, 10.0});

        OptimizationResult result = optimizer.optimize(paraboloid, initialGuess);

        assertFalse(result.converged(), "L'optimisation ne devrait pas avoir convergé");
        assertEquals(TerminationCriterion.MAX_ITERATIONS_REACHED, result.terminationReason());
        assertEquals(maxIterations, result.iterations());
    }

    @Test
    @DisplayName("Devrait minimiser une fonction en 1D f(x) = (x - 3)^2 + 2")
    void shouldMinimizeOneDimensionalFunction() {
        MultivariateFunction f = v -> Math.pow(v.getValue(0) - 3.0, 2) + 2.0;

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-6, 100);
        Vector initialGuess = new ArrayVector(new double[]{0.0});

        OptimizationResult result = optimizer.optimize(f, initialGuess);

        assertTrue(result.converged());
        assertEquals(3.0, result.point().getValue(0), EPSILON);
        assertEquals(2.0, result.minCost(), EPSILON);
    }

    @Test
    @DisplayName("Devrait minimiser une fonction en 3D f(x, y, z) = (x-1)^2 + (y+2)^2 + (z-5)^2")
    void shouldMinimizeThreeDimensionalFunction() {
        MultivariateFunction sphere3D = v ->
                Math.pow(v.getValue(0) - 1.0, 2) +
                        Math.pow(v.getValue(1) + 2.0, 2) +
                        Math.pow(v.getValue(2) - 5.0, 2);

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-6, 500);
        Vector initialGuess = new ArrayVector(new double[]{0.0, 0.0, 0.0});

        OptimizationResult result = optimizer.optimize(sphere3D, initialGuess);

        assertTrue(result.converged());
        assertEquals(1.0, result.point().getValue(0), EPSILON);
        assertEquals(-2.0, result.point().getValue(1), EPSILON);
        assertEquals(5.0, result.point().getValue(2), EPSILON);
    }

    @Test
    @DisplayName("Devrait converger si l'estimation initiale est déjà optimale")
    void shouldConvergeImmediatelyIfInitialGuessIsOptimal() {
        MultivariateFunction paraboloid = v -> v.getValue(0) * v.getValue(0) + v.getValue(1) * v.getValue(1);

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-6, 100);
        Vector initialGuess = new ArrayVector(new double[]{0.0, 0.0});

        OptimizationResult result = optimizer.optimize(paraboloid, initialGuess);

        assertTrue(result.converged());
        assertEquals(0.0, result.minCost(), EPSILON);
    }
}