package org.estimator.interval.bootstrap;

import org.data.Sample;


public record BootstrapResult (

        double pointEstimate,
        Sample bootstrapEstimates

) {
}