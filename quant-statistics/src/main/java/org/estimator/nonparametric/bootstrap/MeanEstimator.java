package org.estimator.nonparametric.bootstrap;


import org.math.vector.Vector;
import org.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MeanEstimator implements VectorEstimator {

        private static final Logger log = LoggerFactory.getLogger(MeanEstimator.class);

        @Override
        public ScalarPointEstimate estimate(Vector sample) {
            if (sample == null || sample.size() < 2) {
                log.error("Sample size must be at least 2 to estimate variance, given dimension: {}",
                        sample == null ? 0 : sample.size());
                throw new IllegalArgumentException("Sample size must be at least 2.");
            }

            int n = sample.size();
            double mean = DescriptiveStatistics.mean(sample);
            double sampleVariance = DescriptiveStatistics.variance(sample);

            // Variance de l'estimateur (se^2) : Var(Mean) = s^2 / n
            double estimatorVariance = sampleVariance / n;

            log.trace("Estimated mean: {} with estimator variance: {} (n={})", mean, estimatorVariance, n);

            return new ScalarPointEstimate(mean, estimatorVariance, n);
        }
}
