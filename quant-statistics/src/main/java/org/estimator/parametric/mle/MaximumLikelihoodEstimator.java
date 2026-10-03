package org.estimator.parametric.mle;

import org.data.Sample;
import org.estimator.nonparametric.bootstrap.VectorPointEstimate;
import org.estimator.parametric.mle.NegativeLogLikelihoodFunction;
import org.estimator.parametric.model.ParametricModel;
import org.math.function.FiniteDifferenceHessianCalculator;
import org.math.function.MultivariateFunction;
import org.math.matrix.Matrix;
import org.math.matrix.SymmetricMatrix;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.NelderMeadOptimizer;
import org.math.optimizer.OptimizationResult;
import org.math.vector.Vector;



public class MaximumLikelihoodEstimator {

    private final MultivariateOptimizer optimizer;

    public MaximumLikelihoodEstimator() {
        this(new NelderMeadOptimizer(1e-2, 200));
    }

    public MaximumLikelihoodEstimator(MultivariateOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public MleResult estimate(ParametricModel<?> parametricModel, Sample datas, Vector initialGuess) throws ConvergenceException {

        if (!parametricModel.isValidParameterSet(initialGuess)){
            throw new ConvergenceException("Wrong dimension initial guess: " + initialGuess.toString());
        }

        MultivariateFunction nllFunction = new NegativeLogLikelihoodFunction(datas, parametricModel);
        OptimizationResult result = optimizer.optimize(nllFunction, initialGuess);
        if (!result.converged()){
            throw new ConvergenceException("Fail to converge: " + result.terminationReason());
        }

        Vector thetaHat = result.point();

        int sampleSize = datas.size();

        // Variance de l'estimateur via l'inverse de la Hessienne (Information de Fisher)
        FiniteDifferenceHessianCalculator finiteDifferenceHessianCalculator = new FiniteDifferenceHessianCalculator();

        SymmetricMatrix hessian = finiteDifferenceHessianCalculator.computeHessian(nllFunction, thetaHat);
        Matrix covarianceMatrix = hessian.invert();
        VectorPointEstimate vectorPointEstimate = new VectorPointEstimate(thetaHat, covarianceMatrix, sampleSize);
        return new MleResult(vectorPointEstimate, -result.minCost());
    }
}