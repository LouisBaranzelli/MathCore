package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;

public class DenseMatrix extends AbstractMatrix {

    private final int rows;
    private final int cols;
    private final double[] values; // Row-major order

    public DenseMatrix(int rows, int cols, double[] values) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be strictly positive");
        }
        if (values == null || values.length != rows * cols) {
            throw new IllegalArgumentException(
                    String.format("Invalid array length: expected %d (rows x cols), got %d",
                            rows * cols, values == null ? 0 : values.length)
            );
        }
        this.rows = rows;
        this.cols = cols;
        this.values = values.clone();
    }

    public DenseMatrix(double[][] rowValues) {
        if (rowValues == null || rowValues.length == 0 || rowValues[0].length == 0) {
            throw new IllegalArgumentException("Matrix data cannot be empty or null");
        }
        this.rows = rowValues.length;
        this.cols = rowValues[0].length;
        this.values = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            if (rowValues[r].length != cols) {
                throw new IllegalArgumentException("All rows in matrix must have the same length");
            }
            System.arraycopy(rowValues[r], 0, this.values, r * cols, cols);
        }
    }

    // Constructeur interne sans vérification ni copie (fast-path)
    DenseMatrix(int rows, int cols, double[] values, boolean unsafeUnchecked) {
        this.rows = rows;
        this.cols = cols;
        this.values = values;
    }

    @Override public int rowCount() { return rows; }
    @Override public int columnCount() { return cols; }

    @Override
    public double get(int row, int col) {
        checkIndices(row, col);
        return values[row * cols + col];
    }


    @Override
    public Matrix add(Matrix other) {
        if (other instanceof DenseMatrix denseOther) {
            checkSameDimensions(other);
            double[] result = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = this.values[i] + denseOther.values[i];
            }
            return new DenseMatrix(rows, cols, result, true);
        }
        return super.add(other);
    }

    @Override
    public Matrix subtract(Matrix other) {
        if (other instanceof DenseMatrix denseOther) {
            checkSameDimensions(other);
            double[] result = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = this.values[i] - denseOther.values[i];
            }
            return new DenseMatrix(rows, cols, result, true);
        }
        return super.subtract(other);
    }

    @Override
    public Matrix multiply(double scalar) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = this.values[i] * scalar;
        }
        return new DenseMatrix(rows, cols, result, true);
    }

    @Override
    public Matrix multiply(Matrix other) {
        // Fast-path ikj cache-friendly si l'autre est aussi une DenseMatrix
        if (other instanceof DenseMatrix denseOther) {
            if (this.cols != denseOther.rows) {
                throw new IllegalArgumentException(
                        String.format("Matrix multiplication dimension mismatch: %dx%d * %dx%d",
                                this.rows, this.cols, denseOther.rows, denseOther.cols)
                );
            }
            int targetCols = denseOther.cols;
            double[] result = new double[this.rows * targetCols];

            for (int r = 0; r < this.rows; r++) {
                int rowOffset = r * this.cols;
                int targetRowOffset = r * targetCols;
                for (int k = 0; k < this.cols; k++) {
                    double a = this.values[rowOffset + k];
                    int otherRowOffset = k * targetCols;
                    for (int c = 0; c < targetCols; c++) {
                        result[targetRowOffset + c] += a * denseOther.values[otherRowOffset + c];
                    }
                }
            }
            return new DenseMatrix(this.rows, targetCols, result, true);
        }
        // Fallback vers AbstractMatrix si "other" est une Sparse/Symmetric/etc.
        return super.multiply(other);
    }

    @Override
    public Vector multiply(Vector vector) {
        if (this.cols != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix-Vector dimension mismatch: matrix has %d columns, vector has size %d",
                            this.cols, vector.size())
            );
        }
        double[] result = new double[this.rows];
        for (int r = 0; r < this.rows; r++) {
            double sum = 0.0;
            int rowOffset = r * this.cols;
            for (int c = 0; c < this.cols; c++) {
                sum += values[rowOffset + c] * vector.getValue(c);
            }
            result[r] = sum;
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector getRow(int row) {
        checkRowIndex(row);
        double[] rowData = new double[cols];
        System.arraycopy(values, row * cols, rowData, 0, cols); // Optimisation via memcpy
        return new ArrayVector(rowData);
    }

    @Override
    public Matrix transpose() {
        double[] result = new double[values.length];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[c * rows + r] = values[r * cols + c];
            }
        }
        return new DenseMatrix(cols, rows, result, true);
    }

    private void checkIndices(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for matrix size %dx%d", row, col, rows, cols)
            );
        }
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rows, cols);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}