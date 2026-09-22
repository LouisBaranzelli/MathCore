package org.estimator.parametric.moment;

import org.estimator.parametric.moment.LogNormalMethodOfMomentsEstimator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.distributions.monovariate.continuous.LogNormalDistribution;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LogNormalMethodOfMomentsEstimatorTest {

    private LogNormalMethodOfMomentsEstimator estimator;

    @BeforeEach
    void setUp() {
        estimator = new LogNormalMethodOfMomentsEstimator();
    }

    @Test
    @DisplayName("Test déterministe avec valeurs exactes calculées à la main")
    void testFitWithKnownSmallSample() {
        // Échantillon : [1.0, 2.0, 4.0]
        // m1 = (1 + 2 + 4) / 3 = 7 / 3 ≈ 2.333333333
        // m2 = (1 + 4 + 16) / 3 = 21 / 3 = 7.0
        // m2 / m1^2 = 7 / (49 / 9) = 63 / 49 = 9 / 7
        // varianceOfLogs = ln(9 / 7) ≈ 0.251314428
        // sigma = sqrt(ln(9 / 7)) ≈ 0.501312705
        // meanOfLogs = ln(7 / 3) - 0.5 * ln(9 / 7) ≈ 0.722950882
        Vector data = new ArrayVector(1.0, 2.0, 4.0);

        LogNormalDistribution fitted = estimator.fit(data);

        double expectedM1 = 7.0 / 3.0;
        double expectedM2 = 7.0;
        double expectedVarianceOfLogs = Math.log(expectedM2 / (expectedM1 * expectedM1));
        double expectedSigma = Math.sqrt(expectedVarianceOfLogs);
        double expectedMu = Math.log(expectedM1) - 0.5 * expectedVarianceOfLogs;

        assertEquals(expectedMu, fitted.getMu(), 1e-9, "Le paramètre mu estimé est incorrect.");
        assertEquals(expectedSigma, fitted.getSigma(), 1e-9, "Le paramètre sigma estimé est incorrect.");
    }

    @Test
    @DisplayName("Test asymptotique avec un grand échantillon log-normal généré (N = 100 000)")
    void testFitWithLargeGeneratedSample() {
        double expectedMu = 1.5;   // Paramètre mu de la loi normale sous-jacente
        double expectedSigma = 0.5; // Paramètre sigma de la loi normale sous-jacente
        int sampleSize = 100_000;

        // Génération de X = exp(mu + sigma * Z) avec Z ~ N(0, 1)
        Random random = new Random(42); // Seed fixe pour la reproductibilité
        double[] values = new double[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            double normalSample = expectedMu + expectedSigma * random.nextGaussian();
            values[i] = Math.exp(normalSample);
        }
        Vector data = new ArrayVector(values);

        LogNormalDistribution fitted = estimator.fit(data);

        // Tolérance adaptée à la variance empirique de l'échantillon
        assertEquals(expectedMu, fitted.getMu(), 0.02, "Le mu estimé s'écarte trop de la valeur théorique.");
        assertEquals(expectedSigma, fitted.getSigma(), 0.02, "Le sigma estimé s'écarte trop de la valeur théorique.");
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException pour des données invalides ou insuffisantes")
    void testFitInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(null));
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(new ArrayVector(2.0)));
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si la moyenne empirique est négative ou nulle")
    void testFitNonPositiveMean() {
        Vector negativeData = new ArrayVector(-1.0, -2.0, -0.5);
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(negativeData));
    }
}