package org.estimator.parametric.mle;

import org.estimator.parametric.model.ParametricModel;
import org.math.function.MultivariateFunction;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.OptimizationResult;
import org.math.vector.Vector;
import org.data.Sample;

public class MaximumLikelihoodEstimator <T extends ParametricModel<T>> {

    private final MultivariateOptimizer optimizer;

    public MaximumLikelihoodEstimator(MultivariateOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public Vector estimate(ParametricModel<T> parametricModel, Sample data, Vector initialGuess) {
        MultivariateFunction nllFunction = new NegativeLogLikelihoodFunction(data, parametricModel);
        OptimizationResult result = optimizer.optimize(nllFunction, initialGuess);
        if (!result.converged()){
            throw new ConvergenceException("Fail to converge: " + result.terminationReason());
        }
        return result.point();
    }
}