package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;


public class DenseMatrix implements Matrix {

    private final int rows;
    private final int cols;
    private final double[] values; // Tableau à plat (row-major order)


    public DenseMatrix(int rows, int cols, double[] values) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be strictly positive");
        }
        if (values == null || values.length != rows * cols) {
            throw new IllegalArgumentException(
                    String.format("Invalid array length: expected %d (rows x cols), got %d", rows * cols, values == null ? 0 : values.length)
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

    private DenseMatrix(int rows, int cols, double[] values, boolean unsafeUnchecked) {
        this.rows = rows;
        this.cols = cols;
        this.values = values;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int columnCount() {
        return cols;
    }

    @Override
    public double get(int row, int col) {
        checkIndices(row, col);
        return values[row * cols + col];
    }

    @Override
    public Matrix add(Matrix other) {
        checkSameDimensions(other);
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = this.values[i] + other.get(i / cols, i % cols);
        }
        // Si l'autre est aussi une DenseMatrix, on évite le get(r,c) lourd
        if (other instanceof DenseMatrix denseOther) {
            for (int i = 0; i < values.length; i++) {
                result[i] = this.values[i] + denseOther.values[i];
            }
        }
        return new DenseMatrix(rows, cols, result, true);
    }

    @Override
    public Matrix subtract(Matrix other) {
        checkSameDimensions(other);
        double[] result = new double[values.length];
        if (other instanceof DenseMatrix denseOther) {
            for (int i = 0; i < values.length; i++) {
                result[i] = this.values[i] - denseOther.values[i];
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                result[i] = this.values[i] - other.get(i / cols, i % cols);
            }
        }
        return new DenseMatrix(rows, cols, result, true);
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
        if (this.cols != other.rowCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix multiplication dimension mismatch: %dx%d * %dx%d",
                            this.rows, this.cols, other.rowCount(), other.columnCount())
            );
        }

        int targetCols = other.columnCount();
        double[] result = new double[this.rows * targetCols];

        // Multiplication matricielle optimisée (Row-major)
        if (other instanceof DenseMatrix denseOther) {
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
        } else {
            for (int r = 0; r < this.rows; r++) {
                for (int c = 0; c < targetCols; c++) {
                    double sum = 0.0;
                    for (int k = 0; k < this.cols; k++) {
                        sum += get(r, k) * other.get(k, c);
                    }
                    result[r * targetCols + c] = sum;
                }
            }
        }

        return new DenseMatrix(this.rows, targetCols, result, true);
    }

    @Override
    public Vector multiply(Vector vector) {
        if (this.cols != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix-Vector dimension mismatch: matrix has %d columns, vector has size %d", this.cols, vector.size())
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
    public Matrix transpose() {
        double[] result = new double[values.length];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[c * rows + r] = values[r * cols + c];
            }
        }
        return new DenseMatrix(cols, rows, result, true);
    }

    @Override
    public Vector getRow(int row) {
        if (row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException(String.format("Row index %d out of bounds for row count %d", row, rows));
        }
        double[] rowData = new double[cols];
        System.arraycopy(values, row * cols, rowData, 0, cols);
        return new ArrayVector(rowData);
    }

    @Override
    public Vector getColumn(int col) {
        if (col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(String.format("Column index %d out of bounds for column count %d", col, cols));
        }
        double[] colData = new double[rows];
        for (int r = 0; r < rows; r++) {
            colData[r] = values[r * cols + col];
        }
        return new ArrayVector(colData);
    }


    private void checkIndices(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for matrix size %dx%d", row, col, rows, cols)
            );
        }
    }

    private void checkSameDimensions(Matrix other) {
        if (this.rows != other.rowCount() || this.cols != other.columnCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimension mismatch: expected %dx%d, got %dx%d",
                            this.rows, this.cols, other.rowCount(), other.columnCount())
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix other)) return false;

        // 1. Vérification des dimensions
        if (this.rowCount() != other.rowCount() || this.columnCount() != other.columnCount()) {
            return false;
        }

        // 2. Fast-path si c'est aussi une DenseMatrix
        if (other instanceof DenseMatrix denseOther) {
            return Arrays.equals(this.values, denseOther.values);
        }

        // 3. Comparaison élément par élément pour les autres implémentations de Matrix
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Double.compare(this.get(r, c), other.get(r, c)) != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rows, cols);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
