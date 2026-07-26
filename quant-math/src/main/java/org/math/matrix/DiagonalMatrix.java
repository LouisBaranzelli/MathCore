package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Objects;

public class DiagonalMatrix implements Matrix {

    private final double[] diagonal; // Uniquement N éléments en mémoire O(N)

    public DiagonalMatrix(double[] diagonal) {
        if (diagonal == null || diagonal.length == 0) {
            throw new IllegalArgumentException("Diagonal data cannot be empty or null");
        }
        this.diagonal = diagonal.clone(); // Copie défensive
    }

    private DiagonalMatrix(double[] diagonal, boolean unsafeUnchecked) {
        this.diagonal = diagonal;
    }

    @Override
    public int rowCount() {
        return diagonal.length;
    }

    @Override
    public int columnCount() {
        return diagonal.length;
    }

    @Override
    public double get(int row, int col) {
        checkIndices(row, col);
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
            return new DiagonalMatrix(result, true); // Reste une matrice diagonale
        }

        // Si l'autre matrice est dense, l'addition produit une matrice dense
        int n = diagonal.length;
        double[] result = new double[n * n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                double val = other.get(r, c);
                if (r == c) {
                    val += this.diagonal[r];
                }
                result[r * n + c] = val;
            }
        }
        return new DenseMatrix(n, n, result);
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

        int n = diagonal.length;
        double[] result = new double[n * n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                double val = (r == c ? this.diagonal[r] : 0.0) - other.get(r, c);
                result[r * n + c] = val;
            }
        }
        return new DenseMatrix(n, n, result);
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
        checkMultiplicationDimensions(other);

        int n = diagonal.length;
        int targetCols = other.columnCount();

        // 1. Fast path: Diagonale x Diagonale -> O(N)
        if (other instanceof DiagonalMatrix diagOther) {
            double[] result = new double[n];
            for (int i = 0; i < n; i++) {
                result[i] = this.diagonal[i] * diagOther.diagonal[i];
            }
            return new DiagonalMatrix(result, true);
        }

        // 2. Fast path: Diagonale x Dense -> O(N x C) au lieu de O(N x N x C)
        double[] result = new double[n * targetCols];
        for (int r = 0; r < n; r++) {
            double d = this.diagonal[r];
            int rowOffset = r * targetCols;
            for (int c = 0; c < targetCols; c++) {
                result[rowOffset + c] = d * other.get(r, c);
            }
        }
        return new DenseMatrix(n, targetCols, result);
    }

    @Override
    public Vector multiply(Vector vector) {
        if (this.diagonal.length != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix-Vector dimension mismatch: matrix size %d, vector size %d", diagonal.length, vector.size())
            );
        }

        // Produit matrice-vecteur en O(N)
        double[] result = new double[diagonal.length];
        for (int i = 0; i < diagonal.length; i++) {
            result[i] = diagonal[i] * vector.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public Matrix transpose() {
        return this;
    }

    @Override
    public Vector getRow(int row) {
        checkRowIndex(row);
        double[] rowData = new double[diagonal.length];
        rowData[row] = diagonal[row]; // Un seul élément non nul
        return new ArrayVector(rowData);
    }

    @Override
    public Vector getColumn(int col) {
        checkColIndex(col);
        double[] colData = new double[diagonal.length];
        colData[col] = diagonal[col]; // Un seul élément non nul
        return new ArrayVector(colData);
    }

    /**
     * Méthode spécifique aux matrices diagonales : Inversion directe en O(N).
     */
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


    private void checkIndices(int row, int col) {
        checkRowIndex(row);
        checkColIndex(col);
    }

    private void checkRowIndex(int row) {
        if (row < 0 || row >= diagonal.length) {
            throw new IndexOutOfBoundsException(String.format("Row index %d out of bounds for matrix size %d", row, diagonal.length));
        }
    }

    private void checkColIndex(int col) {
        if (col < 0 || col >= diagonal.length) {
            throw new IndexOutOfBoundsException(String.format("Column index %d out of bounds for matrix size %d", col, diagonal.length));
        }
    }

    private void checkSameDimensions(Matrix other) {
        if (this.diagonal.length != other.rowCount() || this.diagonal.length != other.columnCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimension mismatch: expected %dx%d, got %dx%d",
                            diagonal.length, diagonal.length, other.rowCount(), other.columnCount())
            );
        }
    }

    private void checkMultiplicationDimensions(Matrix other) {
        if (this.diagonal.length != other.rowCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrix multiplication dimension mismatch: %dx%d * %dx%d",
                            diagonal.length, diagonal.length, other.rowCount(), other.columnCount())
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix other)) return false;

        if (this.rowCount() != other.rowCount() || this.columnCount() != other.columnCount()) {
            return false;
        }

        // Fast-path entre deux matrices diagonales : O(N)
        if (other instanceof DiagonalMatrix diagOther) {
            return Arrays.equals(this.diagonal, diagOther.diagonal);
        }

        // Comparaison élément par élément pour d'autres types de matrices
        for (int r = 0; r < diagonal.length; r++) {
            for (int c = 0; c < diagonal.length; c++) {
                if (Double.compare(this.get(r, c), other.get(r, c)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(diagonal.length, diagonal.length);
        result = 31 * result + Arrays.hashCode(diagonal);
        return result;
    }
}