package org.estimator.nonparametric.bootstrap;

import org.math.vector.Vector;
import org.descriptive.DescriptiveStatistics;

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
public final class TrimmedMeanEstimator implements VectorEstimator {

    private final double alpha;

    public TrimmedMeanEstimator(double alpha) {
        if (alpha < 0.0 || alpha >= 0.5) {
            throw new IllegalArgumentException(
                    "La proportion de tronquage alpha doit être dans [0.0, 0.5[. Reçu : " + alpha
            );
        }
        this.alpha = alpha;
    }

    @Override
    public ScalarPointEstimate estimate(Vector sample) {
        Objects.requireNonNull(sample, "L'échantillon ne peut pas être nul.");
        if (sample.size() == 0) {
            throw new IllegalArgumentException("L'échantillon ne peut pas être vide.");
        }

        if (alpha == 0.0) {
            double sampleVariance = DescriptiveStatistics.variance(sample);
            double mean = DescriptiveStatistics.mean(sample);
            return new ScalarPointEstimate(mean, sampleVariance, sample.size());
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

        double mean = computeMean(sorted, k, n - k);

        double winsorizedVariance = computeWinsorizedVariance(sorted, k, mean);
        double effectiveTrimRatio = 1.0 - 2.0 * ((double) k / n);
        double adjustedVariance = winsorizedVariance / (effectiveTrimRatio * effectiveTrimRatio);
        return new ScalarPointEstimate(mean, adjustedVariance,  sample.size());

    }

    private static double computeMean(double[] array, int startInclusive, int endExclusive) {
        double sum = 0.0;
        for (int i = startInclusive; i < endExclusive; i++) {
            sum += array[i];
        }
        return sum / (endExclusive - startInclusive);
    }

    private static double computeWinsorizedVariance(double[] sorted, int k, double trimmedMean) {
        int n = sorted.length;
        double sumSquaredDiff = 0.0;

        double lowerBoundValue = sorted[k];
        double upperBoundValue = sorted[n - k - 1];

        sumSquaredDiff += k * Math.pow(lowerBoundValue - trimmedMean, 2);

        for (int i = k; i < n - k; i++) {
            sumSquaredDiff += Math.pow(sorted[i] - trimmedMean, 2);
        }

        sumSquaredDiff += k * Math.pow(upperBoundValue - trimmedMean, 2);

        return sumSquaredDiff / (n - 1);
    }



}