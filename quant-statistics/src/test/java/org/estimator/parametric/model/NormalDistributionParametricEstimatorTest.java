package org.estimator.parametric.model;

import org.estimator.parametric.model.NormalDistributionParametricEstimator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.distributions.monovariate.continuous.NormalDistribution;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class NormalDistributionParametricEstimatorTest {

    private NormalDistributionParametricEstimator estimator;

    @BeforeEach
    void setUp() {
        estimator = new NormalDistributionParametricEstimator();
    }

    @Test
    @DisplayName("Test déterministe avec valeurs exactes calculées à la main")
    void testFitWithKnownSmallSample() {
        // Échantillon : [10.0, 12.0, 14.0]
        // m1 = 12.0
        // m2 = (100 + 144 + 196) / 3 = 146.66666666666666
        // Variance = 146.66666666666666 - 144 = 2.66666666666666
        // Sigma = sqrt(8 / 3) ≈ 1.632993161855452
        Vector data = new ArrayVector(10.0, 12.0, 14.0);

        NormalDistribution fitted = estimator.fit(data);

        double expectedMean = 12.0;
        double expectedSigma = Math.sqrt(8.0 / 3.0);

        assertEquals(expectedMean, fitted.getMu(), 1e-9, "La moyenne estimée est incorrecte.");
        assertEquals(expectedSigma, fitted.getSigma(), 1e-9, "L'écart-type estimé est incorrect.");
    }

    @Test
    @DisplayName("Test asymptotique avec un grand échantillon aléatoire (N = 100 000)")
    void testFitWithLargeGeneratedSample() {
        double expectedMu = 5.0;
        double expectedSigma = 2.0;
        int sampleSize = 100_000;

        // Génération par Box-Muller / Random.nextGaussian()
        Random random = new Random(42); // Seed fixe pour la reproductibilité du build
        double[] values = new double[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            values[i] = expectedMu + expectedSigma * random.nextGaussian();
        }
        Vector data = new ArrayVector(values);

        NormalDistribution fitted = estimator.fit(data);

        // Tolérance de 1e-2 adaptée à la taille d'échantillon N = 100 000 (erreur type ~ sigma / sqrt(N))
        assertEquals(expectedMu, fitted.getMu(), 0.02, "La moyenne estimée s'écarte trop de la valeur théorique.");
        assertEquals(expectedSigma, fitted.getSigma(), 0.02, "L'écart-type estimé s'écarte trop de la valeur théorique.");
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si les données sont insuffisantes ou nulles")
    void testFitInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(null));
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(new ArrayVector(10.0)));
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si la variance est nulle")
    void testFitZeroVariance() {
        // Échantillon constant : variance empirique = 0
        Vector data = new ArrayVector(5.0, 5.0, 5.0);

        assertThrows(IllegalArgumentException.class, () -> estimator.fit(data));
    }
}