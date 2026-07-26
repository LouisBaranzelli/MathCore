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
}
