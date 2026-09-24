package org.estimator.parametric.mle;

import org.math.optimizer.OptimizationResult;
import org.math.vector.Vector;

public record LikelihoodResult(Vector theta, double logVraisemblanceMax, OptimizationResult optimizationResult) {
}
