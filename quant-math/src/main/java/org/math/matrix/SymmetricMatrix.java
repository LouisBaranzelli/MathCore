package org.math.matrix;


public interface SymmetricMatrix extends Matrix {
    /**
     * Retourne la dimension de la matrice carrée (rowCount == columnCount).
     */
    int getDimension();

    @Override
    default Matrix transpose() {
        return this;
    }
}