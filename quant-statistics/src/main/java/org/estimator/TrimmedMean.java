package org.estimator;

import org.math.vector.Vector;
import org.statistics.probability.DescriptiveStatistics;

import java.util.Arrays;
import java.util.Objects;

/**
 * Estimateur robuste de la moyenne tronquée (Trimmed Mean).
 * <p>
 * Supprime une proportion {@code alpha} des observations les plus petites
 * et les plus grandes avant de calculer la moyenne empirique du reste.
 * </p>
 * <p>
 * Cet estimateur permet d'atténuer l'impact des valeurs aberrantes (outliers)
 * ou des bruits de microstructure sur des séries temporelles.
 * </p>
 */
public final class TrimmedMean implements VectorEstimator {

    private final double alpha;

    public TrimmedMean(double alpha) {
        if (alpha < 0.0 || alpha >= 0.5) {
            throw new IllegalArgumentException(
                    "La proportion de tronquage alpha doit être dans [0.0, 0.5[. Reçu : " + alpha
            );
        }
        this.alpha = alpha;
    }

    @Override
    public Double estimate(Vector sample) {
        Objects.requireNonNull(sample, "L'échantillon ne peut pas être nul.");
        if (sample.size() == 0) {
            throw new IllegalArgumentException("L'échantillon ne peut pas être vide.");
        }

        if (alpha == 0.0) {
            return DescriptiveStatistics.mean(sample);
        }

        double[] sorted = sample.toArray();
        Arrays.sort(sorted);

        int n = sorted.length;
        int k = (int) Math.floor(n * alpha);

        int remainingElements = n - 2 * k;
        if (remainingElements <= 0) {
            throw new IllegalArgumentException(
                    String.format("L'échantillon de taille %d est trop petit pour un alpha de %f.", n, alpha)
            );
        }

        return computeMean(sorted, k, n - k);

    }

    private static double computeMean(double[] array, int startInclusive, int endExclusive) {
        double sum = 0.0;
        for (int i = startInclusive; i < endExclusive; i++) {
            sum += array[i];
        }
        return sum / (endExclusive - startInclusive);
    }

}