package org.estimator.parametric.mle;

import org.estimator.parametric.model.ParametricModel;
import org.math.function.MultivariateFunction;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.NelderMeadOptimizer;
import org.math.optimizer.OptimizationResult;
import org.math.vector.Vector;
import org.data.Sample;

public class MaximumLikelihoodEstimator {

    private final MultivariateOptimizer optimizer;

    public MaximumLikelihoodEstimator() {
        this(new NelderMeadOptimizer(1e-2, 200));
    }
    
    public MaximumLikelihoodEstimator(MultivariateOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public LikelihoodResult estimate(ParametricModel<?> parametricModel, Sample datas, Vector initialGuess) throws ConvergenceException {

        if (!parametricModel.isValidParameterSet(initialGuess)){
            throw new ConvergenceException("Wrong dimension initial guess: " + initialGuess.toString());
        }

        MultivariateFunction nllFunction = new NegativeLogLikelihoodFunction(datas, parametricModel);
        OptimizationResult result = optimizer.optimize(nllFunction, initialGuess);
        if (!result.converged()){
            throw new ConvergenceException("Fail to converge: " + result.terminationReason());
        }

        return new LikelihoodResult(result.point(), -result.minCost(), result);
    }
}