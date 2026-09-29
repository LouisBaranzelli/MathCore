package org.math.function;

import org.math.matrix.DenseSymmetricMatrix;
import org.math.matrix.MatrixTools;
import org.math.matrix.SymmetricMatrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiniteDifferenceHessianCalculator {

    private static final Logger log = LoggerFactory.getLogger(FiniteDifferenceHessianCalculator.class);
    private static final double EPSILON_CUBE_ROOT = Math.cbrt(1.11e-16); // ~ 4.8e-6

    public SymmetricMatrix computeHessian(MultivariateFunction function, Vector point) {
        log.debug("Computing numerical Hessian at point: {}", point);

        int dim = point.size();
        double[] x = point.toArray();
        double fCenter = function.evaluate(point);

        double[] h = new double[dim];
        for (int i = 0; i < dim; i++) {
            h[i] = EPSILON_CUBE_ROOT * Math.max(Math.abs(x[i]), 1.0);
        }

        double[] packedValue = new double[MatrixTools.packedSize(dim)];

        double[] work = x.clone();

        for (int i = 0; i < dim; i++) {
            double originalXi = x[i];

            work[i] = originalXi + h[i];
            double fPlus = function.evaluate(ArrayVector.of(work));

            work[i] = originalXi - h[i];
            double fMinus = function.evaluate(ArrayVector.of(work));

            work[i] = originalXi; // Restauration de l'état initial

            double hii = (fPlus - 2.0 * fCenter + fMinus) / (h[i] * h[i]);
            packedValue[MatrixTools.upperTriangleIndex(i, i, dim)] = hii;

            for (int j = i + 1; j < dim; j++) {
                double originalXj = x[j];

                // f(x + hi, x + hj)
                work[i] = originalXi + h[i];
                work[j] = originalXj + h[j];
                double fPP = function.evaluate(ArrayVector.of(work));

                // f(x + hi, x - hj)
                work[j] = originalXj - h[j];
                double fPM = function.evaluate(ArrayVector.of(work));

                // f(x - hi, x + hj)
                work[i] = originalXi - h[i];
                work[j] = originalXj + h[j];
                double fMP = function.evaluate(ArrayVector.of(work));

                // f(x - hi, x - hj)
                work[j] = originalXj - h[j];
                double fMM = function.evaluate(ArrayVector.of(work));

                // Restauration du tampon de travail
                work[i] = originalXi;
                work[j] = originalXj;

                double hij = (fPP - fPM - fMP + fMM) / (4.0 * h[i] * h[j]);
                packedValue[MatrixTools.upperTriangleIndex(i, j, dim)] = hij;
            }
        }

        return new DenseSymmetricMatrix(dim, packedValue);
    }
}