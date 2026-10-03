package org.statistics.estimator.interval.bootstrap;

import org.statistics.data.Sample;
import org.statistics.estimator.nonparametric.bootstrap.PointEstimate;


public record BootstrapResult (

        PointEstimate<Double> pointEstimate,
        Sample bootstrapEstimates

) {
}