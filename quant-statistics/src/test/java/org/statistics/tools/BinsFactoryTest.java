package org.statistics.tools;

import org.junit.jupiter.api.Test;
import org.math.vector.Vector;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;

import static org.junit.jupiter.api.Assertions.*;

class BinsFactoryTest {

    @Test
    void testCreateEqualProbabilityBinsWithNormalDistribution() {
        NormalDistribution normal = new NormalDistribution(0.0, 1.0);
        int k = 4;

        Vector bounds = BinsFactory.createEqualProbabilityBins(normal, k);

        assertEquals(k + 1, bounds.size(), "Le nombre de bornes doit être égal à k + 1");

        assertEquals(Double.NEGATIVE_INFINITY, bounds.getValue(0));
        assertEquals(Double.POSITIVE_INFINITY, bounds.getValue(k));

        assertEquals(0.0, bounds.getValue(2), 1e-9);

        for (int i = 0; i < bounds.size() - 1; i++) {
            assertTrue(bounds.getValue(i) < bounds.getValue(i + 1), "Les bornes doivent être strictement croissantes");
        }
    }

    @Test
    void testCreateEqualProbabilityBinsInvalidK() {
        NormalDistribution normal = new NormalDistribution(0.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> {
            BinsFactory.createEqualProbabilityBins(normal, 1);
        }, "Un nombre de bacs inférieur à 2 doit lever une exception.");

        assertThrows(IllegalArgumentException.class, () -> {
            BinsFactory.createEqualProbabilityBins(normal, 0);
        }, "Un nombre de bacs nul doit lever une exception.");
    }
}