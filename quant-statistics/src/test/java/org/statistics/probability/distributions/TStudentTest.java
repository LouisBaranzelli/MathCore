package org.statistics.probability.distributions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TStudentTest {

    private static final double EPSILON = 1e-5;

    @Nested
    @DisplayName("Tests des Getters et de l'initialisation")
    class InitializationTests {

        @Test
        @DisplayName("Validation des getters")
        void testGetters() {
            TStudent t = new TStudent(5.0, 2.0, 1.5);
            assertEquals(5.0, t.getDegreesOfFreedom(), EPSILON);
            assertEquals(2.0, t.getLocation(), EPSILON);
            assertEquals(1.5, t.getScale(), EPSILON);
        }

        @Test
        @DisplayName("Constructeur par défaut centrée-réduite")
        void testDefaultConstructor() {
            TStudent t = new TStudent(10.0);
            assertEquals(10.0, t.getDegreesOfFreedom(), EPSILON);
            assertEquals(0.0, t.getLocation(), EPSILON);
            assertEquals(1.0, t.getScale(), EPSILON);
        }

        @Test
        @DisplayName("Exceptions sur paramètres invalides")
        void testInvalidParameters() {
            assertThrows(IllegalArgumentException.class, () -> new TStudent(0.0));
            assertThrows(IllegalArgumentException.class, () -> new TStudent(-1.0));
            assertThrows(IllegalArgumentException.class, () -> new TStudent(5.0, 0.0, 0.0));
            assertThrows(IllegalArgumentException.class, () -> new TStudent(5.0, 0.0, -2.0));
        }
    }

    @Nested
    @DisplayName("Tests de la Densité (PDF)")
    class DensityTests {

        @Test
        @DisplayName("Densité au sommet (x = mu)")
        void testDensityAtCenter() {
            // Pour df=1 (Cauchy) : f(0) = 1 / pi approx 0.3183098
            TStudent tCauchy = new TStudent(1.0);
            assertEquals(1.0 / Math.PI, tCauchy.density(0.0), EPSILON);

            // Symétrie de la densité autour de mu
            TStudent tGen = new TStudent(4.0, 10.0, 2.0);
            assertEquals(tGen.density(10.0 - 1.5), tGen.density(10.0 + 1.5), EPSILON);
        }

        @Test
        @DisplayName("Densité toujours strictement positive")
        void testDensityPositivity() {
            TStudent t = new TStudent(3.0);
            assertTrue(t.density(-100.0) > 0.0);
            assertTrue(t.density(0.0) > 0.0);
            assertTrue(t.density(100.0) > 0.0);
        }
    }

    @Nested
    @DisplayName("Tests de la Fonction de Répartition (CDF)")
    class CdfTests {

        @Test
        @DisplayName("CDF aux points clés et symétrie")
        void testCdfProperties() {
            TStudent t = new TStudent(5.0, 0.0, 1.0);

            // Médiane à x = 0
            assertEquals(0.5, t.cdf(0.0), EPSILON);

            // P(X <= -x) + P(X <= x) = 1.0
            assertEquals(1.0, t.cdf(-2.0) + t.cdf(2.0), EPSILON);

            // Asymptotes
            assertTrue(t.cdf(-1000.0) < 1e-4);
            assertTrue(t.cdf(1000.0) > 1.0 - 1e-4);
        }

        @Test
        @DisplayName("CDF avec passage dans la branche de symétrie de la Bêta Incomplète")
        void testCdfSymmetryBranch() {
            TStudent t = new TStudent(2.0);
            // Teste z > 0 et z < 0 pour forcer x > (a+1)/(a+b+2) et x <= ...
            double cdfPos = t.cdf(3.5);
            double cdfNeg = t.cdf(-3.5);

            assertEquals(1.0, cdfPos + cdfNeg, EPSILON);
        }
    }

    @Nested
    @DisplayName("Tests de la Fonction Quantile (inverseCdf)")
    class InverseCdfTests {

        @Test
        @DisplayName("Quantiles aux bornes absolues p=0.0 et p=1.0")
        void testInverseCdfInfinities() {
            TStudent t = new TStudent(4.0);
            assertEquals(Double.NEGATIVE_INFINITY, t.inverseCdf(0.0));
            assertEquals(Double.POSITIVE_INFINITY, t.inverseCdf(1.0));
            assertEquals(0.0, t.inverseCdf(0.5), EPSILON);
        }

        @Test
        @DisplayName("Cohérence réciproque : inverseCdf(cdf(x)) == x")
        void testRoundTrip() {
            TStudent t = new TStudent(6.0, 0.02, 0.05);

            double[] testValues = {-0.10, -0.02, 0.02, 0.05, 0.12};
            for (double x : testValues) {
                double p = t.cdf(x);
                assertEquals(x, t.inverseCdf(p), EPSILON);
            }
        }

        @Test
        @DisplayName("Exceptions sur probabilités invalides")
        void testInvalidProbabilities() {
            TStudent t = new TStudent(3.0);
            assertThrows(IllegalArgumentException.class, () -> t.inverseCdf(-0.0001));
            assertThrows(IllegalArgumentException.class, () -> t.inverseCdf(1.0001));
        }
    }
}