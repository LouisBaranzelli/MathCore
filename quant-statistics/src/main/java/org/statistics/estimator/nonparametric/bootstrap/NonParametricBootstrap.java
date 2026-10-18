package org.statistics.estimator.nonparametric.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.estimator.interval.bootstrap.BootstrapResult;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.data.Sample;
import org.statistics.tools.PerformanceMonitor;


import java.util.Objects;
import java.util.random.RandomGenerator;

public class NonParametricBootstrap {

    private static final Logger log = LoggerFactory.getLogger(NonParametricBootstrap.class);

    private final int resampleCount;
    private final RandomGenerator rng;

    public NonParametricBootstrap(int resampleCount, RandomGenerator rng) {
        if (resampleCount < 100) {
            log.error("Attempted initialization with insufficient resample count: {}", resampleCount);
            throw new IllegalArgumentException("resample size < 100.");
        }
        this.resampleCount = resampleCount;
        this.rng = Objects.requireNonNull(rng);
        log.debug("NonParametricBootstrap initialized with resampleCount={}", resampleCount);
    }

    public BootstrapResult run(
            Estimator<Double, Vector> estimator,
            Vector data) {
        int n = data.size();
        if (n < 5) {
            log.error("Sample size too small for bootstrap: n={}", n);
            throw new IllegalArgumentException("Sample size < 5 for the bootstrap.");
        }

        log.info("Starting Non-Parametric Bootstrap: data size={}, resample count={}", n, resampleCount);

        return PerformanceMonitor.measure(log, "Execution: Non-Parametric Bootstrap", () -> {
            PointEstimate<Double> originalEstimate = estimator.estimate(data);
            double[] estimates = new double[resampleCount];

            for (int b = 0; b < resampleCount; b++) {
                double[] buffer = new double[n];
                for (int i = 0; i < n; i++) {
                    buffer[i] = data.getValue(rng.nextInt(n));
                }
                estimates[b] = estimator.estimate(new ArrayVector(buffer)).value();
            }

            log.debug("Non-Parametric Bootstrap completed successfully.");
            return new BootstrapResult(originalEstimate, new Sample(estimates));
        });
    }
}