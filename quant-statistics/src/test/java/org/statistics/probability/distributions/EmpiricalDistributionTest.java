package org.statistics.probability.distributions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.statistics.probability.definitions.Sample;

import static org.junit.jupiter.api.Assertions.*;

class EmpiricalDistributionTest {

    private static final double EPSILON = 1e-9;

    @Nested
    @DisplayName("Tests CDF")
    class CdfTests {

        @Test
        @DisplayName("CDF sur un échantillon sans doublons")
        void testCdfWithoutDuplicates() {
            // Echantillon : [1.0, 2.0, 3.0, 4.0] (N = 4)
            Sample sample = new Sample(new double[]{4.0, 1.0, 3.0, 2.0});
            EmpiricalDistribution dist = new EmpiricalDistribution(sample);

            // Avant le minimum
            assertEquals(0.0, dist.cdf(0.5), EPSILON);

            // Sur les points exacts
            assertEquals(0.25, dist.cdf(1.0), EPSILON);
            assertEquals(0.50, dist.cdf(2.0), EPSILON);
            assertEquals(0.75, dist.cdf(3.0), EPSILON);
            assertEquals(1.00, dist.cdf(4.0), EPSILON);

            // Entre les points (intermédiaires)
            assertEquals(0.25, dist.cdf(1.5), EPSILON); // 1 point <= 1.5
            assertEquals(0.50, dist.cdf(2.8), EPSILON); // 2 points <= 2.8

            // Au-delà du maximum
            assertEquals(1.00, dist.cdf(5.0), EPSILON);
        }

        @Test
        @DisplayName("CDF avec doublons (vérification du saut complet)")
        void testCdfWithDuplicates() {
            // Echantillon : [1.0, 2.0, 2.0, 2.0, 5.0] (N = 5)
            Sample sample = new Sample(new double[]{2.0, 1.0, 2.0, 5.0, 2.0});
            EmpiricalDistribution dist = new EmpiricalDistribution(sample);

            assertEquals(0.2, dist.cdf(1.0), EPSILON);
            // 4 points sur 5 sont <= 2.0 (1.0, 2.0, 2.0, 2.0)
            assertEquals(0.8, dist.cdf(2.0), EPSILON);
            assertEquals(1.0, dist.cdf(5.0), EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests Inverse CDF (Quantiles)")
    class InverseCdfTests {

        private EmpiricalDistribution dist;

        @BeforeEach
        void setUp() {
            // Echantillon trié : [10.0, 20.0, 30.0, 40.0, 50.0] (N = 5)
            Sample sample = new Sample(new double[]{50.0, 10.0, 30.0, 20.0, 40.0});
            dist = new EmpiricalDistribution(sample);
        }

        @Test
        @DisplayName("Quantiles aux bornes p=0.0 et p=1.0")
        void testInverseCdfBoundaries() {
            assertEquals(10.0, dist.inverseCdf(0.0), EPSILON); // Min
            assertEquals(50.0, dist.inverseCdf(1.0), EPSILON); // Max
        }

        @Test
        @DisplayName("Quantiles pour des valeurs p intermédiaires")
        void testInverseCdfNominal() {
            // N = 5
            // p = 0.1 -> ceil(0.5) - 1 = 0 -> 10.0
            assertEquals(10.0, dist.inverseCdf(0.1), EPSILON);

            // p = 0.2 -> ceil(1.0) - 1 = 0 -> 10.0
            assertEquals(10.0, dist.inverseCdf(0.2), EPSILON);

            // p = 0.5 (médiane) -> ceil(2.5) - 1 = 2 -> 30.0
            assertEquals(30.0, dist.inverseCdf(0.5), EPSILON);

            // p = 0.9 -> ceil(4.5) - 1 = 4 -> 50.0
            assertEquals(50.0, dist.inverseCdf(0.9), EPSILON);
        }

        @Test
        @DisplayName("Exception si p est hors de [0, 1]")
        void testInverseCdfInvalidProbability() {
            assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(-0.01));
            assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(1.01));
        }
    }

    @Nested
    @DisplayName("Tests de validation à la construction")
    class ValidationTests {

        @Test
        @DisplayName("Exception si Sample est null")
        void testNullSample() {
            assertThrows(IllegalArgumentException.class, () -> new EmpiricalDistribution(null));
        }
    }
}