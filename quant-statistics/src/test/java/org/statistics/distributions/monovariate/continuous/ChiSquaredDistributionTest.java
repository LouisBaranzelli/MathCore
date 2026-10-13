package org.statistics.distributions.monovariate.continuous;

import org.math.probability.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChiSquaredDistributionTest {

    private static final double TOLERANCE = 1e-5;

    @Test
    void testInvalidDegreesOfFreedom() {
        boolean exceptionThrown = false;
        try {
            new ChiSquaredDistribution(0);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "Un degré de liberté inférieur ou égal à 0 doit lever une IllegalArgumentException.");
    }

    @Test
    void testDensityForTwoDegreesOfFreedom() {
        // Pour k = 2, équivalent à une loi exponentielle de paramètre lambda = 1/2
        ChiSquaredDistribution dist = new ChiSquaredDistribution(2);
        double expected = 0.5 * Math.exp(-1.0);
        double actual = dist.density(2.0);

        assertTrue(Math.abs(expected - actual) < TOLERANCE,
                () -> String.format("La densité calculée (%f) diffère de la valeur théorique (%f).", actual, expected));
    }

    @ParameterizedTest
    @CsvSource({
            "0.5, 0.219697",
            "1.0, 0.241971",
            "2.0, 0.207554",
            "4.0, 0.107982",
            "6.0, 0.048652"
    })
    void testDensityForThreeDegreesOfFreedom(double x, double expected) {
        ChiSquaredDistribution dist = new ChiSquaredDistribution(3);
        double actual = dist.density(x);

        assertTrue(Math.abs(expected - actual) < TOLERANCE,
                () -> String.format("Échec pour k=3 à x=%.1f : attendu %.6f, obtenu %.6f", x, expected, actual));
    }

    @Test
    void testCdfForTwoDegreesOfFreedom() {
        ChiSquaredDistribution dist = new ChiSquaredDistribution(2);
        double expected = 1.0 - Math.exp(-1.0);
        double actual = dist.cdf(2.0);

        assertTrue(Math.abs(expected - actual) < TOLERANCE,
                () -> String.format("La CDF calculée (%f) diffère de la valeur théorique (%f).", actual, expected));
    }

    @Test
    void testInverseCdfConsistency() {
        ChiSquaredDistribution dist = new ChiSquaredDistribution(4);
        double originalX = 5.0;
        double p = dist.cdf(originalX);
        double recoveredX = dist.inverseCdf(p);

        assertTrue(Math.abs(originalX - recoveredX) < 1e-4,
                () -> String.format("L'inverse de la CDF a retourné %f au lieu de %f.", recoveredX, originalX));
    }
}