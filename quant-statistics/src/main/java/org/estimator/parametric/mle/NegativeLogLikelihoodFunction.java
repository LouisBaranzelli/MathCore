package org.estimator.parametric.mle;

import org.estimator.parametric.model.ParametricModel;
import org.math.function.MultivariateFunction;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.data.Sample;

public class NegativeLogLikelihoodFunction implements MultivariateFunction {

    private static final Logger log = LoggerFactory.getLogger(NegativeLogLikelihoodFunction.class);

    private final Sample data;
    private final ParametricModel<?> model;

    public NegativeLogLikelihoodFunction(Sample data, ParametricModel<?> model) {
        this.data = data;
        this.model = model;
    }

    @Override
    public double evaluate(Vector params) {
        if (!model.isValidParameterSet(params)) {
            log.trace("Parameters out of bounds: {}", params);
            return Double.POSITIVE_INFINITY;
        }

        double logLikelihood = 0.0;
        for (double x : data) {
            double logP = model.logLikelihood(x, params);

            if (Double.isNaN(logP) || Double.isInfinite(logP)) {
                return Double.POSITIVE_INFINITY;
            }
            logLikelihood += logP;
        }

        return -logLikelihood;
    }
}