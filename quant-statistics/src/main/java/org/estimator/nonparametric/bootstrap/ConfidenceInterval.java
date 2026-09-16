package org.estimator.nonparametric.bootstrap;

public record ConfidenceInterval<T>(
        T lowerBound,
        T upperBound,
        double confidenceLevel
) {
    public ConfidenceInterval {
        if (confidenceLevel <= 0.0 || confidenceLevel >= 1.0) {
            throw new IllegalArgumentException("Le niveau de confiance doit être strictement compris entre 0 et 1 (ex: 0.95).");
        }
    }
}