package org.statistics.distributions.monovariate.continuous;

import org.math.Integrator.NumericalIntegrator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/**
 * Distribution continue approchée par une grille discrète,
 * implémentant l'interface ContinuousDistribution.
 */
public class GridDiscretizedDistribution implements ContinuousDistribution {

    private static final Logger log = LoggerFactory.getLogger(GridDiscretizedDistribution.class);

    private final double[] grid;
    private final double[] cdf;
    private final double[] densities;
    private final int gridPoints;

    private GridDiscretizedDistribution(double[] grid, double[] cdf, double[] densities) {
        this.grid = grid;
        this.cdf = cdf;
        this.densities = densities;
        this.gridPoints = grid.length;
    }

    public static GridDiscretizedDistribution fromDensity(
            DoubleUnaryOperator density,
            double thetaMin,
            double thetaMax,
            int gridPoints,
            NumericalIntegrator integrator) {

        log.trace("fromDensity called for range [{}, {}] with {} grid points", thetaMin, thetaMax, gridPoints);
        Objects.requireNonNull(density, "Density cannot be null");
        Objects.requireNonNull(integrator, "Integrator cannot be null");

        double[] grid = new double[gridPoints];
        double[] rawDensities = new double[gridPoints];
        double[] cdf = new double[gridPoints];
        double step = (thetaMax - thetaMin) / (gridPoints - 1);

        grid[0] = thetaMin;
        rawDensities[0] = density.applyAsDouble(thetaMin);
        cdf[0] = 0.0;
        double cumulativeIntegral = 0.0;

        for (int i = 1; i < gridPoints; i++) {
            grid[i] = thetaMin + i * step;
            rawDensities[i] = density.applyAsDouble(grid[i]);
            double a = grid[i - 1];
            double b = grid[i];

            double subArea = integrator.integrate(density::applyAsDouble, a, b);
            cumulativeIntegral += Math.max(0.0, subArea);
            cdf[i] = cumulativeIntegral;
        }

        if (cumulativeIntegral <= 0.0 || Double.isNaN(cumulativeIntegral)) {
            log.error("Total integrated probability mass is invalid: {}", cumulativeIntegral);
            throw new IllegalStateException("Invalid total probability mass computed over grid.");
        }

        for (int i = 0; i < gridPoints; i++) {
            cdf[i] /= cumulativeIntegral;
            rawDensities[i] /= cumulativeIntegral;
        }
        cdf[gridPoints - 1] = 1.0;

        log.debug("GridDiscretizedDistribution successfully built.");
        return new GridDiscretizedDistribution(grid, cdf, rawDensities);
    }

    @Override
    public double cdf(double x) {
        log.trace("cdf called with x: {}", x);
        if (x < grid[0]) {
            return 0.0;
        }
        if (x >= grid[gridPoints - 1]) {
            return 1.0;
        }

        int idx = Arrays.binarySearch(grid, x);
        if (idx >= 0) {
            return cdf[idx];
        }

        int insertionPoint = -idx - 1;
        int idxLow = insertionPoint - 1;
        int idxHigh = insertionPoint;

        double xLow = grid[idxLow];
        double xHigh = grid[idxHigh];
        double cdfLow = cdf[idxLow];
        double cdfHigh = cdf[idxHigh];

        return cdfLow + (x - xLow) / (xHigh - xLow) * (cdfHigh - cdfLow);
    }

    @Override
    public double density(double x) {
        log.trace("density called with x: {}", x);
        if (x < grid[0] || x > grid[gridPoints - 1]) {
            return 0.0;
        }

        int idx = Arrays.binarySearch(grid, x);
        if (idx >= 0) {
            return densities[idx];
        }

        int insertionPoint = -idx - 1;
        int idxLow = insertionPoint - 1;
        int idxHigh = insertionPoint;

        double xLow = grid[idxLow];
        double xHigh = grid[idxHigh];
        double densLow = densities[idxLow];
        double densHigh = densities[idxHigh];

        return densLow + (x - xLow) / (xHigh - xLow) * (densHigh - densLow);
    }

    @Override
    public double inverseCdf(double p) {
        log.trace("inverseCdf called with p: {}", p);
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("Probability p must be between 0.0 and 1.0, got: " + p);
        }

        int idx = Arrays.binarySearch(cdf, p);
        if (idx >= 0) {
            return grid[idx];
        }

        int insertionPoint = -idx - 1;
        if (insertionPoint <= 0) {
            return grid[0];
        }
        if (insertionPoint >= gridPoints) {
            return grid[gridPoints - 1];
        }

        int idxLow = insertionPoint - 1;
        int idxHigh = insertionPoint;

        double uLow = cdf[idxLow];
        double uHigh = cdf[idxHigh];
        double thetaLow = grid[idxLow];
        double thetaHigh = grid[idxHigh];

        if (Math.abs(uHigh - uLow) < 1e-12) {
            return thetaLow;
        }

        return thetaLow + (p - uLow) / (uHigh - uLow) * (thetaHigh - thetaLow);
    }

    @Override
    public String getLabel() {
        return "GridDiscretizedDistribution";
    }

    public Vector sample(int numSamples, Random random) {
        log.trace("sample called with numSamples: {}", numSamples);
        Objects.requireNonNull(random, "Random cannot be null");
        if (numSamples <= 0) {
            throw new IllegalArgumentException("numSamples must be strictly positive");
        }

        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = inverseCdf(random.nextDouble());
        }

        log.debug("Generated {} samples from discretized distribution", numSamples);
        return ArrayVector.of(samples);
    }
}