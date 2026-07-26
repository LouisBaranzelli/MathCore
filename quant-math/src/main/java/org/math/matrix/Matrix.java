package org.math.matrix;

import org.math.vector.Vector;

public interface Matrix {

    // --- Dimensions ---
    int rowCount();
    int columnCount();

    default boolean isSquare() {
        return rowCount() == columnCount();
    }

    double get(int row, int col);

    Matrix add(Matrix other);

    Matrix subtract(Matrix other);

    Matrix multiply(double scalar);

    Matrix multiply(Matrix other);

    Vector multiply(Vector vector);

    Matrix transpose();

    Vector getRow(int row);

    Vector getColumn(int col);
}