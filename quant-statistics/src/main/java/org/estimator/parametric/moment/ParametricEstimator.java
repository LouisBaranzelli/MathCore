package org.estimator.parametric.moment;

import org.math.vector.Vector;
import org.statistics.probability.distributions.monovariate.Distribution;

interface ParametricEstimator<D extends Distribution> {

    D fit(Vector data);
}
