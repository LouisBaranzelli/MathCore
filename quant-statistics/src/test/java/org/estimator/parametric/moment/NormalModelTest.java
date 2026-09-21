package org.estimator.parametric.moment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.probability.distributions.monovariate.NormalDistribution;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalModelTest {

    private static final double EPSILON = 1e-9;
    private NormalModel model;

    @BeforeEach
    void setUp() {
        model = new NormalModel();
    }

    @Test
    void testLogLikelihoodMatchesAnalyticDensity() {
        double mu = 1.5;
        double sigma = 2.0;
        Vector params = new ArrayVector(mu, sigma);
        double x = 2.5;

        NormalDistribution dist = model.createDistribution(params);
        double expectedLogLikelihood = Math.log(dist.density(x));
        double actualLogLikelihood = model.logLikelihood(x, params);

        assertTrue(Math.abs(expectedLogLikelihood - actualLogLikelihood) < EPSILON);
    }

    @Test
    void testInvalidParametersReturnNegativeInfinity() {
        Vector invalidParams = new ArrayVector(0.0, -1.0);
        double logL = model.logLikelihood(1.0, invalidParams);

        assertTrue(Double.isInfinite(logL) && logL < 0);
    }
}