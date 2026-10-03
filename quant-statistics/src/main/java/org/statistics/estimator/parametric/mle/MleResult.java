package org.statistics.estimator.parametric.mle;

import org.statistics.estimator.nonparametric.bootstrap.VectorPointEstimate;
import org.math.vector.Vector;

public record MleResult(
        VectorPointEstimate estimate,
        double logLikelihoodMax
) {
    public Vector parameters() {
        return estimate.value();
    }
}