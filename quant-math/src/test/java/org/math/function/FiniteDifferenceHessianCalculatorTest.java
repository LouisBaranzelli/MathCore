package org.math.function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.matrix.SymmetricMatrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FiniteDifferenceHessianCalculatorTest {

    private FiniteDifferenceHessianCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FiniteDifferenceHessianCalculator();
    }

    @Test
    @DisplayName("Devrait calculer la Hessienne exacte pour la fonction de Rosenbrock au point (1, 2)")
    void testRosenbrockHessian() {
        // f(x, y) = (1 - x)^2 + 100 * (y - x^2)^2
        MultivariateFunction rosenbrock = point -> {
            double x = point.getValue(0);
            double y = point.getValue(1);
            return Math.pow(1.0 - x, 2) + 100.0 * Math.pow(y - x * x, 2);
        };

        // Point d'évaluation (x = 1.0, y = 2.0)
        Vector point = ArrayVector.of(1.0, 2.0);

        // Valeurs théoriques attendues pour H(1, 2)
        double expectedH00 = 402.0;
        double expectedH01 = -400.0;
        double expectedH11 = 200.0;

        // Calcul numérique
        SymmetricMatrix hessian = calculator.computeHessian(rosenbrock, point);

        assertNotNull(hessian);
        assertEquals(2, hessian.getDimension());

        // Tolérance relative/absolue adaptée aux différences finies (~ 1e-4)
        double delta = 1e-4;

        assertEquals(expectedH00, hessian.get(0, 0), delta, "H(0,0) doit valoir 402.0");
        assertEquals(expectedH01, hessian.get(0, 1), delta, "H(0,1) doit valoir -400.0");
        assertEquals(expectedH01, hessian.get(1, 0), delta, "H(1,0) doit respecter la symétrie (-400.0)");
        assertEquals(expectedH11, hessian.get(1, 1), delta, "H(1,1) doit valoir 200.0");
    }

    @Test
    @DisplayName("Devrait calculer la Hessienne d'une forme quadratique simple (Hessienne constante)")
    void testQuadraticFormHessian() {
        // f(x, y, z) = 3x^2 + 2y^2 + z^2 - 4xy + 2xz
        // H = [[6, -4, 2], [-4, 4, 0], [2, 0, 2]] en tout point
        MultivariateFunction quadratic = point -> {
            double x = point.getValue(0);
            double y = point.getValue(1);
            double z = point.getValue(2);
            return 3 * x * x + 2 * y * y + z * z - 4 * x * y + 2 * x * z;
        };

        Vector point = ArrayVector.of(0.5, -1.2, 3.0);
        SymmetricMatrix hessian = calculator.computeHessian(quadratic, point);

        double delta = 1e-4;

        assertEquals(6.0,  hessian.get(0, 0), delta);
        assertEquals(-4.0, hessian.get(0, 1), delta);
        assertEquals(2.0,  hessian.get(0, 2), delta);
        assertEquals(4.0,  hessian.get(1, 1), delta);
        assertEquals(0.0,  hessian.get(1, 2), delta);
        assertEquals(2.0,  hessian.get(2, 2), delta);
    }
}