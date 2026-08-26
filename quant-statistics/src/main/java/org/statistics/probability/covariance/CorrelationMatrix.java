package org.statistics.probability.covariance;

import org.math.matrix.SymmetricMatrix;
import org.math.vector.Vector;
import org.statistics.probability.covariance.CovarianceMatrix;

public interface CorrelationMatrix extends SymmetricMatrix {

    default boolean isValid(double tolerance) {
        int n = getDimension();

        for (int i = 0; i < n; i++) {
            if (Math.abs(get(i, i) - 1.0) > tolerance) {
                return false;
            }
        }

        for (int r = 0; r < n; r++) {
            for (int c = r + 1; c < n; c++) {
                double val = get(r, c);
                if (val < -1.0 - tolerance || val > 1.0 + tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isPositiveSemiDefinite();

//    pour se rapprocher d'une semi-définie positivité quand on a des data empirique
//    CorrelationMatrix toNearestCorrelationMatrix();

    CovarianceMatrix toCovarianceMatrix(Vector standardDeviations);

}