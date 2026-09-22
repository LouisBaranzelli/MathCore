package org.estimator.interval.bootstrap;

import org.data.Sample;
import org.estimator.interval.ConfidenceInterval;

public enum BootstrapIntervalMethod {
    PERCENTILE {
        @Override
        public ConfidenceInterval<Double> calculate(BootstrapResult result, double confidenceLevel) {

            if (confidenceLevel > 1 || confidenceLevel < 0){
                throw  new IllegalArgumentException("Confidence level must be between 0 and 1");
            }

            double alpha = 1.0 - confidenceLevel;
            double lowerQuantile = quantile(result.bootstrapEstimates(), alpha / 2.0);
            double upperQuantile = quantile(result.bootstrapEstimates(), 1.0 - alpha / 2.0);
            return new ConfidenceInterval<>(lowerQuantile, upperQuantile, confidenceLevel);
        }
    },
    PIVOTAL {
        @Override
        public ConfidenceInterval<Double> calculate(BootstrapResult result, double confidenceLevel) {

            if (confidenceLevel > 1 || confidenceLevel < 0){
                throw  new IllegalArgumentException("Confidence level must be between 0 and 1");
            }

            double alpha = 1.0 - confidenceLevel;
            double theta = result.pointEstimate();

            // Inversion pivotale : 2*theta - q_(1-alpha/2) et 2*theta - q_(alpha/2)
            double qLower = quantile(result.bootstrapEstimates(), alpha / 2.0);
            double qUpper = quantile(result.bootstrapEstimates(), 1.0 - alpha / 2.0);

            double lowerBound = 2 * theta - qUpper;
            double upperBound = 2 * theta - qLower;

            return new ConfidenceInterval<>(lowerBound, upperBound, confidenceLevel);
        }
    };

    public abstract ConfidenceInterval<Double> calculate(BootstrapResult result, double confidenceLevel);

    protected double quantile(Sample sortedData, double p) {
        // Implémentation rigoureuse d'interpolation linéaire pour quantile
        double index = p * (sortedData.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        double lowerValue = sortedData.getSorted(lower);
        double upperValue = sortedData.getSorted(upper);
        if (lower == upper) return lowerValue;
        return lowerValue + (index - lower) * (upperValue - lowerValue);
    }
}