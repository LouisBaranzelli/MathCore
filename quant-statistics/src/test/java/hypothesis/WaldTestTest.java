package hypothesis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;

import static org.junit.jupiter.api.Assertions.*;

class WaldTestTest {

    private ContinuousDistribution standardNormal;
    private WaldTest waldTest;

    @BeforeEach
    void setUp() {
        standardNormal = new NormalDistribution(0.0, 1.0);
        waldTest = new WaldTest(standardNormal);
    }

    @Test
    void shouldComputeCorrectStatisticAndPValue() {
        // Estimation = 10.5, SE = 2.0, H0 : theta_0 = 5.0
        // W = (10.5 - 5.0) / 2.0 = 5.5 / 2.0 = 2.75
        HypothesisTestResult result = waldTest.test(10.5, 2.0, 5.0);

        assertTrue(Math.abs(result.testStatistic() - 2.75) < 1e-6);
        // Pour Z = 2.75, la p-value bilatérale est d'environ 0.00595
        assertTrue(result.pValue() < 0.01);
        assertTrue(result.isRejected(0.05)); // Rejeté au seuil de 5%
    }

    @Test
    void shouldNotRejectNullWhenEstimateIsCloseToNull() {
        // Estimation = 5.1, SE = 2.0, H0 : theta_0 = 5.0
        // W = 0.1 / 2.0 = 0.05 -> p-value proche de 1, pas de rejet
        HypothesisTestResult result = waldTest.test(5.1, 2.0, 5.0);

        assertTrue(Math.abs(result.testStatistic() - 0.05) < 1e-6);
        assertTrue(result.pValue() > 0.05);
        assertFalse(result.isRejected(0.05));
    }

    @Test
    void shouldThrowExceptionWhenStandardErrorIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> waldTest.test(10.0, 0.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> waldTest.test(10.0, -1.5, 5.0));
    }

    @Test
    void shouldThrowExceptionWhenDistributionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new WaldTest(null));
    }
}