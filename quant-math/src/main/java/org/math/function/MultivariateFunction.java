package org.math.function;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;


@FunctionalInterface
public interface MultivariateFunction {

    double evaluate(Vector point);

    default Vector gradient(Vector x, double stepSize) {
        int p = x.size();
        double[] grad = new double[p];
        double[] xValues = x.toArray();

        for (int i = 0; i < p; i++) {
            double originalValue = xValues[i];
            xValues[i] = originalValue + stepSize;
            double fPlus = evaluate(new ArrayVector(xValues));
            xValues[i] = originalValue - stepSize;
            double fMinus = evaluate(new ArrayVector(xValues));
            xValues[i] = originalValue;
            grad[i] = (fPlus - fMinus) / (2.0 * stepSize);
        }
        return new ArrayVector(grad);
    }


    default Vector gradient(Vector x) {
        return gradient(x, 1e-5);
    }
}


