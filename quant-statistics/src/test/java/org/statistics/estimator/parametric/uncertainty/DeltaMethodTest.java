package org.statistics.estimator.parametric.uncertainty;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.function.MultivariateFunction;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.estimator.nonparametric.bootstrap.VectorPointEstimate;
import org.statistics.estimator.parametric.model.NormalModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeltaMethodTest {

    private static final double EPSILON = 1e-3;

    @Test
    @DisplayName("Devrait estimer correctement le SE unidimensionnel g(theta) = theta^2")
    void shouldEstimateStandardErrorUnivariate() {
        // g(theta) = theta^2  => g'(theta) = 2*theta
        // Soit theta_hat = 3.0 avec Var(theta_hat) = 0.25 (SE = 0.5)
        // Delta Method: Var(g(theta_hat)) = (g'(3.0))^2 * Var(theta_hat) = (6)^2 * 0.25 = 36 * 0.25 = 9.0
        // SE(g(theta_hat)) = sqrt(9.0) = 3.0

        Vector theta = new ArrayVector(new double[]{3.0});
        Matrix cov = new DenseMatrix(new double[][]{{0.25}});
        VectorPointEstimate estimate = new VectorPointEstimate(theta, cov, 100);

        MultivariateFunction g = point -> Math.pow(point.getValue(0), 2);

        DeltaMethod deltaMethod = new DeltaMethod();
        double se = deltaMethod.estimateStandardError(estimate, g);

        assertEquals(3.0, se, EPSILON);
    }

    @Test
    @DisplayName("Devrait estimer le SE pour le Ratio (ex: Sharpe ratio mu/sigma)")
    void shouldEstimateStandardErrorForRatio() {
        // g(mu, s) = mu / s
        // Grad g(mu, s) = [1/s, -mu/s^2]
        // Pour mu = 2.0, s = 4.0:
        // Grad g = [1/4, -2/16] = [0.25, -0.125]

        Vector theta = new ArrayVector(new double[]{2.0, 4.0});

        // Matrice de covariance diagonale (indépendance): Var(mu) = 0.16, Var(s) = 0.64
        Matrix cov = new DenseMatrix(new double[][]{
                {0.16, 0.0},
                {0.0, 0.64}
        });
        VectorPointEstimate estimate = new VectorPointEstimate(theta, cov, 250);

        MultivariateFunction sharpeRatio = point -> point.getValue(0) / point.getValue(1);

        // Var(g) = (0.25)^2 * 0.16 + (-0.125)^2 * 0.64
        //        = 0.0625 * 0.16 + 0.015625 * 0.64
        //        = 0.01 + 0.01 = 0.02
        // SE = sqrt(0.02) ≈ 0.14142

        DeltaMethod deltaMethod = new DeltaMethod(1e-6);
        double se = deltaMethod.estimateStandardError(estimate, sharpeRatio);

        assertEquals(Math.sqrt(0.02), se, EPSILON);
    }

    @Test
    @DisplayName("Devrait rejeter un stepSize négatif ou nul dans le constructeur")
    void shouldThrowExceptionForInvalidStepSize() {
        assertThrows(IllegalArgumentException.class, () -> new DeltaMethod(0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeltaMethod(-1e-4));
    }
    @Test
    @DisplayName("Devrait estimer la SE du Sharpe Ratio sur un NormalModel avec covariance non nulle")
    void shouldEstimateStandardErrorForSharpeRatioWithCorrelatedParameters() {
        // Given: Modèle Gaussien et fonction Sharpe Ratio (rf = 0.02)
        NormalModel normalModel = new NormalModel();
        double riskFreeRate = 0.02;
        MultivariateFunction sharpeFunction = point -> {
            double expectedReturn = normalModel.mean(point);
            double stdDev = Math.sqrt(normalModel.variance(point));
            return (expectedReturn - riskFreeRate) / stdDev;
        };

        // Paramètres mu = 0.10, sigma = 0.20
        Vector theta = new ArrayVector(0.10, 0.20);

        // Matrice de covariance avec terme croisé Cov(mu, sigma) = 0.0001
        Matrix covMatrix = new DenseMatrix(new double[][]{
                {0.0004, 0.0001},
                {0.0001, 0.0002}
        });

        VectorPointEstimate estimate = new VectorPointEstimate(theta, covMatrix, 500);

        // Value théorique attendue : sqrt(0.0088) ≈ 0.0938083
        double expectedSE = Math.sqrt(0.0088);

        // When
        DeltaMethod deltaMethod = new DeltaMethod(1e-6);
        double actualSE = deltaMethod.estimateStandardError(estimate, sharpeFunction);

        // Then
        assertEquals(expectedSE, actualSE, EPSILON);
    }

}