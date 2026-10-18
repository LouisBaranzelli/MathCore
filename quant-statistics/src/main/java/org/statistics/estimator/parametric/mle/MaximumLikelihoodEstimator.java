package org.statistics.estimator.parametric.mle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.data.Sample;
import org.statistics.estimator.nonparametric.bootstrap.VectorPointEstimate;
import org.statistics.estimator.parametric.model.ParametricModel;
import org.math.function.FiniteDifferenceHessianCalculator;
import org.math.function.MultivariateFunction;
import org.math.matrix.Matrix;
import org.math.matrix.SymmetricMatrix;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.NelderMeadOptimizer;
import org.math.optimizer.OptimizationResult;
import org.math.vector.Vector;
import org.statistics.tools.PerformanceMonitor;


public class MaximumLikelihoodEstimator {

    private static final Logger log = LoggerFactory.getLogger(MaximumLikelihoodEstimator.class);

    private final MultivariateOptimizer optimizer;

    public MaximumLikelihoodEstimator() {
        this(new NelderMeadOptimizer(1e-2, 200));
    }

    public MaximumLikelihoodEstimator(MultivariateOptimizer optimizer) {
        this.optimizer = optimizer;
        log.debug("MaximumLikelihoodEstimator initialized with optimizer: {}", optimizer.getClass().getSimpleName());
    }

    public MleResult estimate(ParametricModel<?> parametricModel, Sample datas, Vector initialGuess) throws ConvergenceException {

        if (!parametricModel.isValidParameterSet(initialGuess)){
            log.error("Wrong dimension or invalid initial guess for parametric model: {}", initialGuess);
            throw new ConvergenceException("Wrong dimension initial guess: " + initialGuess.toString());
        }

        log.info("Starting Maximum Likelihood Estimation for model: {}, sample size={}", parametricModel.getClass().getSimpleName(), datas.size());

        return PerformanceMonitor.measure(log, "Execution: Maximum Likelihood Estimation", () -> {
            MultivariateFunction nllFunction = new NegativeLogLikelihoodFunction(datas, parametricModel);
            OptimizationResult result = optimizer.optimize(nllFunction, initialGuess);

            if (!result.converged()){
                log.error("Optimization failed to converge: {}", result.terminationReason());
                throw new ConvergenceException("Fail to converge: " + result.terminationReason());
            }

            log.debug("Optimization converged successfully. Minimum cost (negative log-likelihood): {}", result.minCost());

            Vector thetaHat = result.point();
            int sampleSize = datas.size();

            // Variance estimation via the inverse of the Hessian (Fisher Information)
            FiniteDifferenceHessianCalculator finiteDifferenceHessianCalculator = new FiniteDifferenceHessianCalculator();

            SymmetricMatrix hessian = finiteDifferenceHessianCalculator.computeHessian(nllFunction, thetaHat);
            Matrix covarianceMatrix = hessian.invert();

            log.debug("Hessian computed and inverted successfully for covariance matrix calculation.");

            VectorPointEstimate vectorPointEstimate = new VectorPointEstimate(thetaHat, covarianceMatrix, sampleSize);
            return new MleResult(vectorPointEstimate, -result.minCost());
        });
    }
}