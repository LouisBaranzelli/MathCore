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

    Matrix invert();

    Vector getRow(int row);

    Vector getColumn(int col);

    default String print() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < rowCount(); row++) {
            sb.append("[");
            for (int col = 0; col < columnCount(); col++) {
                if (col > 0) {
                    sb.append(", ");
                }
                sb.append(get(row, col));
            }
            sb.append("]");

            if (row < rowCount() - 1) {
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}