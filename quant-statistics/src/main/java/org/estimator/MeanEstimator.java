package org.estimator;

import org.math.vector.Vector;
import org.statistics.probability.DescriptiveStatistics;

public final class MeanEstimator implements VectorEstimator {


    /**
     * Calcule l'intervalle de confiance basé sur l'approximation gaussienne du TCL.
     */

    @Override
    public Double estimate(Vector sample) {
        return DescriptiveStatistics.mean(sample);
    }
}