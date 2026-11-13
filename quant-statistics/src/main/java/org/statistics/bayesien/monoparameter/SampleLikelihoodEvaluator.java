package org.statistics.bayesien.monoparameter;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;

public final class SampleLikelihoodEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SampleLikelihoodEvaluator.class);

    private SampleLikelihoodEvaluator() {
    }

    /**
     * Calcule la log-vraisemblance globale d'un échantillon i.i.d. stocké dans un Vector :
     * l_n(theta) = sum_{i=0}^{N-1} log f(x_i | theta)
     */
    public static <D> double evaluateLogLikelihood(Vector data, ParametricModel<D> model, Vector params) {
        log.trace("evaluateLogLikelihood called with data size: {}, params: {}", data != null ? data.size() : 0, params);

        Objects.requireNonNull(data, "Data vector cannot be null");
        Objects.requireNonNull(model, "Model cannot be null");


        if (!model.isValidParameterSet(params)) {
            log.warn("Invalid parameters passed to log-likelihood evaluation.");
            throw  new IllegalArgumentException("Invalid parameters passed to log-likelihood evaluation.");
        }

        double totalLogLikelihood = 0.0;
        for (int i = 0; i < data.size(); i++) {
            double x = data.getValue(i);
            totalLogLikelihood += model.logLikelihood(x, params);
        }

        log.debug("Evaluated total log-likelihood: {} for vector sample size {}", totalLogLikelihood, data.size());
        return totalLogLikelihood;
    }
}