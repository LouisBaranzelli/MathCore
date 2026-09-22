package org.distributions.monovariate.continuous;

import org.distributions.monovariate.continuous.LogNormalDistribution;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogNormalDistributionTest {

    private static final double EPSILON = 1e-6;

    @Test
    void testDensityAndCdfBoundaryCondition() {
        LogNormalDistribution logNormal = new LogNormalDistribution(0.0, 1.0);

        assertTrue(logNormal.density(0.0) == 0.0);
        assertTrue(logNormal.density(-1.0) == 0.0);
        assertTrue(logNormal.cdf(0.0) == 0.0);
        assertTrue(logNormal.cdf(-1.0) == 0.0);
    }

    @Test
    void testCdfAndInverseCdfConsistency() {
        LogNormalDistribution logNormal = new LogNormalDistribution(0.5, 0.25);
        double x = 2.0;

        double p = logNormal.cdf(x);
        double reconstructedX = logNormal.inverseCdf(p);

        assertTrue(Math.abs(x - reconstructedX) < EPSILON);
    }

    @Test
    void testInvalidSigma() {
        boolean exceptionThrown = false;
        try {
            new LogNormalDistribution(0.0, 0.0);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}