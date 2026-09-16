package org.estimator.nonparametric.bootstrap;

import org.math.vector.Vector;
import org.statistics.probability.definitions.Sample;


public record BootstrapResult (

        double pointEstimate,
        Sample bootstrapEstimates

) {
}