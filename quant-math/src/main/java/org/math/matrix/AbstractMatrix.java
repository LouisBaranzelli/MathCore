package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractMatrix implements Matrix {

    @Override
    public Matrix add(Matrix other) {
        checkSameDimensions(other);
        int rows = rowCount();
        int cols = columnCount();
        double[] result = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            int offset = r * cols;
            for (int c = 0; c < cols; c++) {
                result[offset + c] = this.get(r, c) + other.get(r, c);
            }
        }
        return new DenseMatrix(rows, cols, result);
    }

    @Override
    public Matrix subtract(Matrix other) {
        checkSameDimensions(other);
        int rows = rowCount();
        int cols = columnCount();
        double[] result = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            int offset = r * cols;
            for (int c = 0; c < cols; c++) {
                result[offset + c] = this.get(r, c) - other.get(r, c);
            }
        }
        return new DenseMatrix(rows, cols, result);
    }

    @Override
    public Matrix multiply(double scalar) {
        int rows = rowCount();
        int cols = columnCount();
        double[] result = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            int offset = r * cols;
            for (int c = 0; c < cols; c++) {
                result[offset + c] = this.get(r, c) * scalar;
            }
        }
        return new DenseMatrix(rows, cols, result);
    }

    @Override
    public Matrix multiply(Matrix other) {
        if (this.columnCount() != other.rowCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix multiplication dimension mismatch: %dx%d * %dx%d",
                            rowCount(), columnCount(), other.rowCount(), other.columnCount())
            );
        }

        int rows = this.rowCount();
        int commonDim = this.columnCount();
        int cols = other.columnCount();
        double[] result = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            int offset = r * cols;
            for (int c = 0; c < cols; c++) {
                double sum = 0.0;
                for (int k = 0; k < commonDim; k++) {
                    sum += this.get(r, k) * other.get(k, c);
                }
                result[offset + c] = sum;
            }
        }
        return new DenseMatrix(rows, cols, result);
    }

    @Override
    public Vector multiply(Vector vector) {
        if (this.columnCount() != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix-Vector dimension mismatch: matrix cols is %d, vector size is %d",
                            columnCount(), vector.size())
            );
        }

        int rows = rowCount();
        int cols = columnCount();
        double[] result = new double[rows];

        for (int r = 0; r < rows; r++) {
            double sum = 0.0;
            for (int c = 0; c < cols; c++) {
                sum += this.get(r, c) * vector.getValue(c);
            }
            result[r] = sum;
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector getRow(int row) {
        checkRowIndex(row);
        int cols = columnCount();
        double[] rowData = new double[cols];
        for (int c = 0; c < cols; c++) {
            rowData[c] = this.get(row, c);
        }
        return new ArrayVector(rowData);
    }

    @Override
    public Vector getColumn(int col) {
        checkColumnIndex(col);
        int rows = rowCount();
        double[] colData = new double[rows];
        for (int r = 0; r < rows; r++) {
            colData[r] = this.get(r, col);
        }
        return new ArrayVector(colData);
    }

    protected void checkSameDimensions(Matrix other) {
        if (this.rowCount() != other.rowCount() || this.columnCount() != other.columnCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimension mismatch: expected %dx%d, got %dx%d",
                            rowCount(), columnCount(), other.rowCount(), other.columnCount())
            );
        }
    }

    protected void checkRowIndex(int row) {
        if (row < 0 || row >= rowCount()) {
            throw new IndexOutOfBoundsException(
                    String.format("Row index %d out of bounds for row count %d", row, rowCount())
            );
        }
    }

    protected void checkColumnIndex(int col) {
        if (col < 0 || col >= columnCount()) {
            throw new IndexOutOfBoundsException(
                    String.format("Column index %d out of bounds for column count %d", col, columnCount())
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix other)) return false;

        int rows = rowCount();
        int cols = columnCount();

        if (rows != other.rowCount() || cols != other.columnCount()) {
            return false;
        }

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
        int rows = rowCount();
        int cols = columnCount();
        int result = Objects.hash(rows, cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result = 31 * result + Double.hashCode(this.get(r, c));
            }
        }
        return result;
    }
}