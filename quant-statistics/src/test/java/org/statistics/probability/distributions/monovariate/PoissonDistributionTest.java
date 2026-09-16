package org.statistics.probability.distributions.monovariate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoissonDistributionTest {

    private static final double EPSILON = 1e-6;

    @Test
    void testPmfProperties() {
        PoissonDistribution poisson = new PoissonDistribution(3.0);

        // P(X = 2) = (3^2 * exp(-3)) / 2! = 9 * 0.049787 / 2 = 0.2240418
        double expectedPmf2 = 0.22404180765538775;
        double actualPmf2 = poisson.pmf(2);

        assertTrue(Math.abs(expectedPmf2 - actualPmf2) < EPSILON);
        assertTrue(poisson.pmf(-1) == 0.0);
    }

    @Test
    void testCdfProperties() {
        PoissonDistribution poisson = new PoissonDistribution(3.0);

        // CDF(2) = P(X=0) + P(X=1) + P(X=2)
        double p0 = poisson.pmf(0);
        double p1 = poisson.pmf(1);
        double p2 = poisson.pmf(2);
        double expectedCdf2 = p0 + p1 + p2;

        assertTrue(Math.abs(expectedCdf2 - poisson.cdf(2.0)) < EPSILON);
        assertTrue(poisson.cdf(-0.5) == 0.0);
    }

    @Test
    void testInvalidLambda() {
        boolean exceptionThrown = false;
        try {
            new PoissonDistribution(0.0);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}