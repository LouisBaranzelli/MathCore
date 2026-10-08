package org.statistics.estimator.parametric.uncertainty;


import org.math.function.MultivariateFunction;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.estimator.nonparametric.bootstrap.VectorPointEstimate;



public class DeltaMethod implements UncertaintyEstimator {

    private final double stepSize; // Pas pour le calcul du gradient par différences finies
    private static Logger logger = LoggerFactory.getLogger(MultivariateFunction.class);

    public DeltaMethod(double stepSize) {
        if (stepSize <= 0) {
            logger.warn("Step size must be strictly positive.");
            throw new IllegalArgumentException("Step size must be strictly positive.");
        }
        this.stepSize = stepSize;
    }

    public DeltaMethod() {
        this(1e-5);
    }


    /*
    Le result doit contenir des argument adapter à la fonction: ex pour trouver le ration sharp
    si le result provien du MLE de Loi Student-$t$ $(\mu, s, \nu)$, alors la fonction doit est adapté à ces argument
     */
    @Override
    public double estimateStandardError(VectorPointEstimate result, MultivariateFunction gFunction) {

        Vector theta = result.value();
        Matrix covMatrix = result.covarianceMatrix();

        Vector gradVector = gFunction.gradient(theta, stepSize);
        Matrix gradRow = new DenseMatrix(1, gradVector.size(), gradVector.toArray());

        Matrix gradTranspose = gradRow.transpose(); // (p x 1)
        Matrix varianceMatrix = gradRow.multiply(covMatrix.multiply(gradTranspose));

        double variance = varianceMatrix.get(0, 0);

        if (variance < 0) {
            logger.warn("Calculated variance is negative due to numerical instability.");
            throw new ArithmeticException("Calculated variance is negative due to numerical instability.");
        }
        return Math.sqrt(variance);
    }

}