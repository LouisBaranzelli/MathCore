package org.statistics.distributions.monovariate.discrete;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BernoulliDistributionTest {

    private static final double EPSILON = 1e-9;

    @Test
    @DisplayName("Should throw IllegalArgumentException when p is out of bounds [0, 1]")
    void testInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new BernoulliDistribution(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new BernoulliDistribution(1.1));
    }

    @Test
    @DisplayName("Should return correct p getter value")
    void testGetP() {
        BernoulliDistribution dist = new BernoulliDistribution(0.3);
        assertEquals(0.3, dist.getP(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "0.4, 0, 0.6", // PMF(0) = 1 - p = 0.6
            "0.4, 1, 0.4", // PMF(1) = p = 0.4
            "0.4, 2, 0.0", // PMF(k > 1) = 0.0
            "0.4, -1, 0.0" // PMF(k < 0) = 0.0
    })
    @DisplayName("Parameterized test for Probability Mass Function (PMF)")
    void testPmf(double p, int k, double expectedPmf) {
        BernoulliDistribution dist = new BernoulliDistribution(p);
        assertEquals(expectedPmf, dist.pmf(k), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "0.3, -1.0, 0.0", // x < 0 => CDF = 0.0
            "0.3, 0.0,  0.7", // 0 <= x < 1 => CDF = 1 - p = 0.7
            "0.3, 0.5,  0.7", // 0 <= x < 1 => CDF = 0.7
            "0.3, 1.0,  1.0", // x >= 1 => CDF = 1.0
            "0.3, 2.5,  1.0"  // x >= 1 => CDF = 1.0
    })
    @DisplayName("Parameterized test for Cumulative Distribution Function (CDF)")
    void testCdf(double p, double x, double expectedCdf) {
        BernoulliDistribution dist = new BernoulliDistribution(p);
        assertEquals(expectedCdf, dist.cdf(x), EPSILON);
    }
}