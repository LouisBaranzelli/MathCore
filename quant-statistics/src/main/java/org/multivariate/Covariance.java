package org.multivariate;

import org.math.matrix.MatrixTools;
import org.math.vector.Vector;
import org.descriptive.DescriptiveStatistics;

public final class Covariance {

    private Covariance() {}

    public static double of(Vector x, Vector y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Vector must have the same size.");
        }
        int n = x.size();
        if (n <= 1) return 0.0;

        double meanX = DescriptiveStatistics.mean(x);
        double meanY = DescriptiveStatistics.mean(y);

        double sumProduct = 0.0;
        for (int i = 0; i < n; i++) {
            sumProduct += (x.getValue(i) - meanX) * (y.getValue(i) - meanY);
        }
        return sumProduct / (n - 1);
    }

    public static CovarianceMatrix of(Vector... vectors) {
        return of(true, vectors); // biasCorrected:  si sous echantillion (c'est notre cas, on divise par N-1 au lizu de N)
    }

    public static CovarianceMatrix of(boolean biasCorrected, Vector... vectors) {
        if (vectors == null || vectors.length < 2) {
            throw new IllegalArgumentException("At least 2 vectors required for the covariance matrix.");
        }

        int p = vectors.length;
        int n = vectors[0].size();

        for (int i = 1; i < p; i++) {
            if (vectors[i].size() != n) {
                throw new IllegalArgumentException("Vectors must have the same size.");
            }
        }

        double[] means = new double[p];
        for (int i = 0; i < p; i++) {
            means[i] = DescriptiveStatistics.mean(vectors[i]);
        }

        double[] packed = new double[MatrixTools.packedSize(p)];
        int idx = 0;
        double divisor = (biasCorrected && n > 1) ? (n - 1) : n;

        for (int r = 0; r < p; r++) {
            Vector vecR = vectors[r];
            double meanR = means[r];

            for (int c = r; c < p; c++) {
                Vector vecC = vectors[c];
                double meanC = means[c];

                double sumProduct = 0.0;
                for (int i = 0; i < n; i++) {
                    sumProduct += (vecR.getValue(i) - meanR) * (vecC.getValue(i) - meanC);
                }

                packed[idx++] = sumProduct / divisor;
            }
        }

        return new DenseCovarianceMatrix(p, packed);
    }
}
