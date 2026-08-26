package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;

public class DiagonalMatrix extends AbstractMatrix implements SymmetricMatrix {

    private final double[] diagonal;

    public DiagonalMatrix(double[] diagonal) {
        if (diagonal == null || diagonal.length == 0) {
            throw new IllegalArgumentException("Diagonal data cannot be empty or null");
        }
        this.diagonal = diagonal.clone();
    }

    private DiagonalMatrix(double[] diagonal, boolean unsafeUnchecked) {
        this.diagonal = diagonal;
    }

    @Override public int rowCount() { return diagonal.length; }
    @Override public int columnCount() { return diagonal.length; }

    @Override
    public boolean isSquare() {
        return super.isSquare();
    }

    @Override
    public double get(int row, int col) {
        checkRowIndex(row);
        checkColumnIndex(col);
        return (row == col) ? diagonal[row] : 0.0;
    }


    @Override
    public Matrix add(Matrix other) {
        checkSameDimensions(other);
        if (other instanceof DiagonalMatrix diagOther) {
            double[] result = new double[diagonal.length];
            for (int i = 0; i < diagonal.length; i++) {
                result[i] = this.diagonal[i] + diagOther.diagonal[i];
            }
            return new DiagonalMatrix(result, true);
        }
        return super.add(other); // Laisse AbstractMatrix gérer le cas général/Dense
    }

    @Override
    public Matrix subtract(Matrix other) {
        checkSameDimensions(other);
        if (other instanceof DiagonalMatrix diagOther) {
            double[] result = new double[diagonal.length];
            for (int i = 0; i < diagonal.length; i++) {
                result[i] = this.diagonal[i] - diagOther.diagonal[i];
            }
            return new DiagonalMatrix(result, true);
        }
        return super.subtract(other);
    }

    @Override
    public Matrix multiply(double scalar) {
        double[] result = new double[diagonal.length];
        for (int i = 0; i < diagonal.length; i++) {
            result[i] = this.diagonal[i] * scalar;
        }
        return new DiagonalMatrix(result, true);
    }

    @Override
    public Matrix multiply(Matrix other) {
        if (this.columnCount() != other.rowCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix multiplication dimension mismatch: %dx%d * %dx%d",
                            diagonal.length, diagonal.length, other.rowCount(), other.columnCount())
            );
        }

        // Fast-path 1: Diagonale x Diagonale -> O(N)
        if (other instanceof DiagonalMatrix diagOther) {
            double[] result = new double[diagonal.length];
            for (int i = 0; i < diagonal.length; i++) {
                result[i] = this.diagonal[i] * diagOther.diagonal[i];
            }
            return new DiagonalMatrix(result, true);
        }

        // Fast-path 2: Diagonale x DenseMatrix -> Mise à l'échelle des lignes
        int n = diagonal.length;
        int targetCols = other.columnCount();
        if (other instanceof DenseMatrix denseOther) {
            double[] result = new double[n * targetCols];
            for (int r = 0; r < n; r++) {
                double factor = diagonal[r];
                int offset = r * targetCols;
                for (int c = 0; c < targetCols; c++) {
                    result[offset + c] = factor * denseOther.get(r, c);
                }
            }
            return new DenseMatrix(n, targetCols, result);
        }

        return super.multiply(other);
    }

    @Override
    public Vector multiply(Vector vector) {
        if (this.diagonal.length != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix-Vector dimension mismatch: matrix size %d, vector size %d", diagonal.length, vector.size())
            );
        }
        double[] result = new double[diagonal.length];
        for (int i = 0; i < diagonal.length; i++) {
            result[i] = diagonal[i] * vector.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public int getDimension() {
        return diagonal.length;
    }

    @Override
    public Matrix transpose() {
        return this;
    }

    @Override
    public Vector getRow(int row) {
        checkRowIndex(row);
        double[] rowData = new double[diagonal.length];
        rowData[row] = diagonal[row];
        return new ArrayVector(rowData);
    }

    @Override
    public Vector getColumn(int col) {
        checkColumnIndex(col);
        double[] colData = new double[diagonal.length];
        colData[col] = diagonal[col];
        return new ArrayVector(colData);
    }

    public DiagonalMatrix inverse() {
        double[] invResult = new double[diagonal.length];
        for (int i = 0; i < diagonal.length; i++) {
            if (Math.abs(diagonal[i]) < 1e-15) {
                throw new ArithmeticException("Cannot invert diagonal matrix: zero element on diagonal at index " + i);
            }
            invResult[i] = 1.0 / diagonal[i];
        }
        return new DiagonalMatrix(invResult, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix other)) return false;

        if (this.rowCount() != other.rowCount() || this.columnCount() != other.columnCount()) {
            return false;
        }

        if (other instanceof DiagonalMatrix diagOther) {
            return Arrays.equals(this.diagonal, diagOther.diagonal);
        }

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(diagonal.length, diagonal.length);
        result = 31 * result + Arrays.hashCode(diagonal);
        return result;
    }
}