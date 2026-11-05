package org.statistics.tools;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;

public class BinsFactory {
    private static final Logger logger = LoggerFactory.getLogger(BinsFactory.class);

    public static Vector createEqualProbabilityBins(ContinuousDistribution distribution, int k) {
        logger.trace("Entering createEqualProbabilityBins with distribution={}, k={}", distribution, k);

        if (k < 2) {
            throw new IllegalArgumentException("Number of bins must be at least 2");
        }

        double[] bounds = new double[k + 1];
        bounds[0] = Double.NEGATIVE_INFINITY;
        bounds[k] = Double.POSITIVE_INFINITY;

        for (int j = 1; j < k; j++) {
            double p = (double) j / k;
            bounds[j] = distribution.inverseCdf(p);
        }

        Vector result = new ArrayVector(bounds);
        logger.debug("Exiting createEqualProbabilityBins with generated bounds size={}", result.size());
        return result;
    }
}
