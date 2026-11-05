package org.statistics.frequentist.estimator.interval.bootstrap;

import org.statistics.data.Sample;
import org.statistics.frequentist.estimator.nonparametric.bootstrap.PointEstimate;


public record BootstrapResult (

        PointEstimate<Double> pointEstimate,
        Sample bootstrapEstimates

) {
}