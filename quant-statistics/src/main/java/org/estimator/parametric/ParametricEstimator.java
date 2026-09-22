package org.estimator.parametric;

import org.math.vector.Vector;
import org.distributions.monovariate.Distribution;

public interface ParametricEstimator<D extends Distribution> {

    D fit(Vector data);
}
