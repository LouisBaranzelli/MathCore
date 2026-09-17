package org.estimator.parametric.moment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.probability.distributions.monovariate.PoissonDistribution;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PoissonMethodOfMomentsEstimatorTest {

    private PoissonMethodOfMomentsEstimator estimator;

    @BeforeEach
    void setUp() {
        estimator = new PoissonMethodOfMomentsEstimator();
    }

    @Test
    @DisplayName("Test déterministe avec valeurs exactes calculées à la main")
    void testFitWithKnownSmallSample() {
        // Échantillon : [1.0, 3.0, 5.0, 3.0]
        // m1 = (1 + 3 + 5 + 3) / 4 = 12 / 4 = 3.0
        Vector data = new ArrayVector(1.0, 3.0, 5.0, 3.0);

        PoissonDistribution fitted = estimator.fit(data);

        double expectedLambda = 3.0;

        assertEquals(expectedLambda, fitted.getLambda(), 1e-9, "Le paramètre lambda estimé est incorrect.");
    }

    @Test
    @DisplayName("Test asymptotique avec un grand échantillon généré (N = 100 000, lambda = 4.5)")
    void testFitWithLargeGeneratedSample() {
        double expectedLambda = 4.5;
        int sampleSize = 100_000;

        // Génération de tirages de Poisson(lambda) via l'algorithme de Knuth
        Random random = new Random(42); // Seed fixe pour la reproductibilité du build
        double[] values = new double[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            values[i] = generatePoissonSample(expectedLambda, random);
        }
        Vector data = new ArrayVector(values);

        PoissonDistribution fitted = estimator.fit(data);

        // Erreur-type ~ sqrt(lambda / N) = sqrt(4.5 / 100 000) ≈ 0.0067
        // Une tolérance de 0.02 garantit le passage du test en CI/CD
        assertEquals(expectedLambda, fitted.getLambda(), 0.02, "Le lambda estimé s'écarte trop de la valeur théorique.");
    }

    @Test
    @DisplayName("Doit accepter un échantillon de taille N = 1 (contrairement à la loi normale/log-normale)")
    void testFitWithSingleDataPoint() {
        Vector data = new ArrayVector(4.0);

        PoissonDistribution fitted = estimator.fit(data);

        assertEquals(4.0, fitted.getLambda(), 1e-9);
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si les données sont insuffisantes ou nulles")
    void testFitInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(null));
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(new ArrayVector()));
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si l'échantillon contient des valeurs négatives")
    void testFitNegativeValues() {
        Vector invalidData = new ArrayVector(2.0, -1.0, 3.0);
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(invalidData));
    }

    @Test
    @DisplayName("Doit lever IllegalArgumentException si la moyenne empirique est nulle")
    void testFitZeroMean() {
        Vector zeroData = new ArrayVector(0.0, 0.0, 0.0);
        assertThrows(IllegalArgumentException.class, () -> estimator.fit(zeroData));
    }

    /**
     * Générateur pseudo-aléatoire de Poisson (Algorithme de Knuth) :
     * Produit K ~ Poisson(lambda) à partir d'une distribution uniforme U(0,1).
     */
    private int generatePoissonSample(double lambda, Random random) {
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1.0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);

        return k - 1;
    }
}