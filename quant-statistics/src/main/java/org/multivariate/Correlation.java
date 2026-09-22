package org.multivariate;

import org.math.vector.Vector;


public final class Correlation {

    private Correlation() {}

    public static double of(Vector x, Vector y) {
        return Covariance.of(new Vector[] {x, y}).toCorrelationMatrix().get(0, 1);
    }

    public static CorrelationMatrix of(Vector... vectors) {
        return Covariance.of(true, vectors).toCorrelationMatrix();
    }

    public static CorrelationMatrix of(boolean biasCorrected, Vector... vectors) {
        return Covariance.of(biasCorrected, vectors).toCorrelationMatrix();
    }
}
