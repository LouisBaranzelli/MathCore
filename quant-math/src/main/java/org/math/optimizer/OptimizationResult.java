package org.math.optimizer;

import org.math.vector.Vector;

import java.util.Objects;

public record OptimizationResult(
        Vector point,
        double minCost,
        boolean converged,
        int iterations,
        String terminationReason
) {
    public OptimizationResult {
        Objects.requireNonNull(point, "point can not be null");
    }
}