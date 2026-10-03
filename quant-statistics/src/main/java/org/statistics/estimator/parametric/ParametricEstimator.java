package org.statistics.estimator.parametric;

import org.math.vector.Vector;
import org.statistics.distributions.monovariate.Distribution;

public interface ParametricEstimator<D extends Distribution> {

    D fit(Vector data);
}
