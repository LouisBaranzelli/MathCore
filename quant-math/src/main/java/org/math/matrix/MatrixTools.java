package org.math.matrix;

public class MatrixTools {

    public static boolean isSymetric(Matrix matrix, int size) {
        double epsilon = 1e-10;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (Math.abs(matrix.get(i, j) - matrix.get(j, i)) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calcule le nombre d'éléments nécessaires pour stocker le triangle d'une
     * matrice symétrique/dimension n au format packed (1D).
     *
     * @param dimension La dimension (nombre de lignes/colonnes) de la matrice.
     * @return La taille du tableau 1D requis (n * (n + 1) / 2).
     */
    public static int packedSize(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("La dimension doit être strictement positive : " + dimension);
        }
        return dimension * (dimension + 1) / 2;
    }

    /**
     * Convertit un index de ligne (r) et de colonne (c) en index 1D dans un tableau
     * packed storage (triangle supérieur, row-major).
     */
    public static int upperTriangleIndex(int row, int col, int dimension) {
        if (row > col) {
            // Pour tirage symétrique si row > col
            int tmp = row;
            row = col;
            col = tmp;
        }
        return row * dimension - (row - 1) * row / 2 + (col - row);
    }
}
