package org.estimator;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;
import java.util.random.RandomGenerator;


public class PercentileBootstrapIntervalEstimator implements IntervalEstimator<Double, Vector> {

    private final Estimator<Double, Vector> pointEstimator;
    private final int resample;
    private final RandomGenerator rng;

    public PercentileBootstrapIntervalEstimator(Estimator<Double, Vector> pointEstimator, int resample, RandomGenerator rng) {
        this.pointEstimator = pointEstimator;
        this.resample = resample;
        this.rng = rng;

        if (resample < 100) {
            throw new IllegalArgumentException("resample to small, should be > 100.");
        }
        Objects.requireNonNull(pointEstimator);
        Objects.requireNonNull(rng);


    }

    @Override
    public ConfidenceInterval<Double> estimateInterval(Vector sample, double confidenceLevel) {
        int n = sample.size();
        if (n < 5) {
            throw new IllegalArgumentException("Taille d'échantillon insuffisante pour le Bootstrap.");
        }

        double[] bootstrapEstimates =  new double[this.resample];
        double[] resampleBuffer = new double[n];

        for (int b = 0; b < this.resample; b++) {
            for (int i = 0; i < n; i++) {
                int randomIndex = this.rng.nextInt(n);
                resampleBuffer[i] = sample.getValue(randomIndex);
            }
            bootstrapEstimates[b] = pointEstimator.estimate(new ArrayVector(resampleBuffer));
        }

        Arrays.sort(bootstrapEstimates);

        double alpha = 1.0 - confidenceLevel;
        int lowerIndex = (int) Math.round((alpha / 2.0) * (this.resample - 1));
        int upperIndex = (int) Math.round((1.0 - alpha / 2.0) * (this.resample - 1));

        return new ConfidenceInterval<>(
                bootstrapEstimates[lowerIndex],
                bootstrapEstimates[upperIndex],
                confidenceLevel
        );
    }

}