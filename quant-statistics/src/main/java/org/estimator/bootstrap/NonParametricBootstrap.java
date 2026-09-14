package org.estimator.bootstrap;

import org.estimator.Estimator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.probability.definitions.Sample;

import java.util.Objects;
import java.util.random.RandomGenerator;

public class NonParametricBootstrap {

    private final int resampleCount;
    private final RandomGenerator rng;

    public NonParametricBootstrap(int resampleCount, RandomGenerator rng) {
        if (resampleCount < 100) {
            throw new IllegalArgumentException("resample size < 100.");
        }
        this.resampleCount = resampleCount;
        this.rng = Objects.requireNonNull(rng);
    }

    public BootstrapResult run(
            Estimator<Double, Vector> estimator,
            Vector data) {

        int n = data.size();
        if (n < 5) {
            throw new IllegalArgumentException("Sample size < 5 for the bootstrap.");
        }
        Double originalEstimate = estimator.estimate(data);
        double[] estimates = new double[resampleCount];

        for (int b = 0; b < resampleCount; b++) {
            double[] buffer = new double[n];
            for (int i = 0; i < n; i++) {
                buffer[i] = data.getValue(rng.nextInt(n));
            }
            estimates[b] = estimator.estimate(new ArrayVector(buffer));
        }

        return new BootstrapResult(originalEstimate, new Sample(estimates));
    }
}