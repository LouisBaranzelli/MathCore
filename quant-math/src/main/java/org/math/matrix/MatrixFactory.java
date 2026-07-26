package org.math.matrix;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class MatrixFactory {

    private MatrixFactory() {
    }


    public static Matrix identity(int size) {
        validatePositiveDimensions(size, size);
        double[] diag = new double[size];
        Arrays.fill(diag, 1.0);
        return new DiagonalMatrix(diag);
    }


    public static Matrix zeros(int rows, int cols) {
        validatePositiveDimensions(rows, cols);
        return new DenseMatrix(rows, cols, new double[rows * cols]);
    }


    public static Matrix ones(int rows, int cols) {
        return fill(rows, cols, 1.0);
    }



    public static Matrix fill(int rows, int cols, double value) {
        validatePositiveDimensions(rows, cols);
        double[] data = new double[rows * cols];
        Arrays.fill(data, value);
        return new DenseMatrix(rows, cols, data);
    }



    public static Matrix diagonal(double[] diagonalValues) {
        return new DiagonalMatrix(diagonalValues);
    }

    public static Matrix random(int rows, int cols) {
        return randomUniform(rows, cols, 0.0, 1.0);
    }


    public static Matrix randomUniform(int rows, int cols, double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be strictly less than max");
        }
        validatePositiveDimensions(rows, cols);

        double[] data = new double[rows * cols];
        Random rng = ThreadLocalRandom.current();
        for (int i = 0; i < data.length; i++) {
            data[i] = min + (max - min) * rng.nextDouble();
        }
        return new DenseMatrix(rows, cols, data);
    }


    public static Matrix randomGaussian(int rows, int cols) {
        return randomGaussian(rows, cols, 0.0, 1.0);
    }


    public static Matrix randomGaussian(int rows, int cols, double mean, double stdDev) {
        validatePositiveDimensions(rows, cols);
        if (stdDev <= 0) {
            throw new IllegalArgumentException("Standard deviation must be strictly positive");
        }

        double[] data = new double[rows * cols];
        Random rng = ThreadLocalRandom.current();
        for (int i = 0; i < data.length; i++) {
            data[i] = mean + stdDev * rng.nextGaussian();
        }
        return new DenseMatrix(rows, cols, data);
    }


    public static Matrix randomUniform(int rows, int cols, double min, double max, long seed) {
        validatePositiveDimensions(rows, cols);
        double[] data = new double[rows * cols];
        Random rng = new Random(seed);
        for (int i = 0; i < data.length; i++) {
            data[i] = min + (max - min) * rng.nextDouble();
        }
        return new DenseMatrix(rows, cols, data);
    }


    private static void validatePositiveDimensions(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException(
                    String.format("Dimensions must be strictly positive (got rows=%d, cols=%d)", rows, cols)
            );
        }
    }
}