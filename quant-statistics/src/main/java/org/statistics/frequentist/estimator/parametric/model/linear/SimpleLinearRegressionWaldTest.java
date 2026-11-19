package org.statistics.frequentist.estimator.parametric.model.linear;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;
import org.statistics.frequentist.hypothesis.HypothesisTestResult;

/**
 * Test de Wald bilatéral asymptotique appliqué à un coefficient de régression linéaire simple.
 */
public class SimpleLinearRegressionWaldTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleLinearRegressionWaldTest.class);
    private final NormalDistribution standardNormal;

    public SimpleLinearRegressionWaldTest(NormalDistribution standardNormal) {
        if (standardNormal == null) {
            throw new IllegalArgumentException("Standard normal distribution must not be null.");
        }
        this.standardNormal = standardNormal;
    }

    /**
     * Teste H0 : beta_1 = nullValue contre H1 : beta_1 != nullValue.
     */
    public HypothesisTestResult testBeta1(SimpleLinearRegressionResult fitResult, double nullValue) {
        log.trace("testBeta1 called with fitResult={}, nullValue={}", fitResult, nullValue);
        HypothesisTestResult result = testCoefficient(fitResult.beta1(), fitResult.beta1StandardError(), nullValue);
        log.debug("testBeta1 completed  pValue={}",  result.pValue());
        return result;
    }

    private HypothesisTestResult testCoefficient(double estimate, double standardError, double nullValue) {
        if (standardError < 0.0) {
            throw new IllegalArgumentException("Standard error must be non-negative: " + standardError);
        }
        double diff = estimate - nullValue;
        if (standardError == 0.0) {
            double stat = (diff == 0.0) ? 0.0 : Double.POSITIVE_INFINITY * Math.signum(diff);
            double pVal = (diff == 0.0) ? 1.0 : 0.0;
            return new HypothesisTestResult(stat, pVal);
        }
        double z = diff / standardError;
        double pValue = Math.clamp(2.0 * (1.0 - standardNormal.cdf(Math.abs(z))), 0.0, 1.0);
        return new HypothesisTestResult(z, pValue);
    }
}