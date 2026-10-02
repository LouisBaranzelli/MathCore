package org.estimator.interval.bootstrap;

import org.data.Sample;
import org.estimator.nonparametric.bootstrap.PointEstimate;


public record BootstrapResult (

        PointEstimate<Double> pointEstimate,
        Sample bootstrapEstimates

) {
}