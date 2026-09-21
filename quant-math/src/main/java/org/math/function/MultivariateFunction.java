package org.math.function;

import org.math.vector.Vector;


@FunctionalInterface
public interface MultivariateFunction {

    double evaluate(Vector point);
}


