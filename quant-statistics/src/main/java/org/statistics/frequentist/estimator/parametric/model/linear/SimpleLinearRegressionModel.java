package org.statistics.frequentist.estimator.parametric.model.linear;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.frequentist.estimator.parametric.model.RegressionModel;

/**
 * Modèle paramétrique pour la régression linéaire simple : r(x) = beta_0 + beta_1 * x
 * avec des résidus gaussiens de variance sigma^2 constante (homoscédasticité).
 *
 * Vecteur de paramètres attendu : [beta_0, beta_1, sigma] (taille 3)
 */
public class SimpleLinearRegressionModel implements RegressionModel {

    private static final Logger log = LoggerFactory.getLogger(SimpleLinearRegressionModel.class);

    private static final double HALF_LN_2PI = 0.5 * Math.log(2.0 * Math.PI);

    @Override
    public boolean isValidParameterSet(Vector params) {
        log.trace("isValidParameterSet called with params: {}", params);

        if (params == null || params.size() != 3) {
            log.debug("Invalid parameter vector size: expected 3 (beta_0, beta_1, sigma), got {}",
                    params == null ? "null" : params.size());
            return false;
        }
        double sigma = params.getValue(2);

        // sigma (écart-type) doit être strictement positif
        boolean valid = !Double.isNaN(params.getValue(0)) && !Double.isInfinite(params.getValue(0))
                && !Double.isNaN(params.getValue(1)) && !Double.isInfinite(params.getValue(1))
                && sigma > 0.0 && !Double.isNaN(sigma) && !Double.isInfinite(sigma);

        if (!valid) {
            log.warn("Invalid parameter values detected in regression model: beta_0={}, beta_1={}, sigma={}",
                    params.getValue(0), params.getValue(1), sigma);
        }

        log.debug("isValidParameterSet evaluated to: {}", valid);
        return valid;
    }

    @Override
    public double predict(double x, Vector params) {
        log.trace("predict called with x: {}, params: {}", x, params);

        if (!isValidParameterSet(params)) {
            log.error("Attempted prediction with invalid parameters: {}", params);
            throw new IllegalArgumentException("Invalid parameter vector for regression prediction.");
        }

        double beta0 = params.getValue(0);
        double beta1 = params.getValue(1);
        double prediction = beta0 + beta1 * x;

        log.debug("predict computed: {}", prediction);
        return prediction;
    }

    @Override
    public double logLikelihood(double y, double x, Vector params) {
        log.trace("logLikelihood called with y: {}, x: {}, params: {}", y, x, params);

        if (!isValidParameterSet(params)) {
            throw new IllegalArgumentException("Invalid parameters " + params.toString());
        }

        double sigma = params.getValue(2);
        double mean = predict(x, params);

        double z = (y - mean) / sigma;

        double logL = -Math.log(sigma) - HALF_LN_2PI - 0.5 * z * z;

        log.debug("logLikelihood computed for point (y={}, x={}): {}", y, x, logL);
        return logL;
    }
}