package org.math.matrix;

import org.math.vector.Vector;

public class DenseSymmetricMatrix extends AbstractMatrix implements SymmetricMatrix {

    private final int dimension;
    private final double[] packedValues;

    public DenseSymmetricMatrix(int dimension, double[] packedValues) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        int expectedLength = dimension * (dimension + 1) / 2;
        if (packedValues.length != expectedLength) {
            throw new IllegalArgumentException(
                    String.format("Packed array length must be %d for dimension %d, got %d",
                            expectedLength, dimension, packedValues.length)
            );
        }
        this.dimension = dimension;
        this.packedValues = packedValues.clone();
    }


    @Override public int getDimension() { return dimension; }
    @Override public int rowCount() { return dimension; }
    @Override public int columnCount() { return dimension; }

    @Override
    public double get(int row, int col) {
        checkIndices(row, col);
        int r = Math.min(row, col);
        int c = Math.max(row, col);
        int index = r * dimension - (r * (r - 1)) / 2 + (c - r);
        return packedValues[index];
    }


    @Override
    public Matrix add(Matrix other) {
        if (other instanceof SymmetricMatrix symOther) {
            checkSameDimensions(other);
            double[] resultPacked = new double[packedValues.length];
            int index = 0;
            for (int r = 0; r < dimension; r++) {
                for (int c = r; c < dimension; c++) {
                    resultPacked[index] = this.packedValues[index] + symOther.get(r, c);
                    index++;
                }
            }
            return new DenseSymmetricMatrix(dimension, resultPacked);
        }
        return super.add(other);
    }

    @Override
    public Matrix subtract(Matrix other) {
        if (other instanceof SymmetricMatrix symOther) {
            checkSameDimensions(other);
            double[] resultPacked = new double[packedValues.length];
            int index = 0;
            for (int r = 0; r < dimension; r++) {
                for (int c = r; c < dimension; c++) {
                    resultPacked[index] = this.packedValues[index] - symOther.get(r, c);
                    index++;
                }
            }
            return new DenseSymmetricMatrix(dimension, resultPacked);
        }
        return super.subtract(other);
    }

    @Override
    public Matrix multiply(double scalar) {
        double[] resultPacked = new double[packedValues.length];
        for (int i = 0; i < packedValues.length; i++) {
            resultPacked[i] = this.packedValues[i] * scalar;
        }
        return new DenseSymmetricMatrix(dimension, resultPacked);
    }

    @Override
    public Vector getColumn(int col) {
        return getRow(col);
    }

    private void checkIndices(int row, int col) {
        if (row < 0 || row >= dimension || col < 0 || col >= dimension) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for dimension %d", row, col, dimension)
            );
        }
    }
}