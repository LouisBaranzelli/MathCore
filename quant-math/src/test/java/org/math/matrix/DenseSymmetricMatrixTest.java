package org.math.matrix;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DenseSymmetricMatrixTest {

    @Test
    void testConstructorAndGet() {
        // 3x3 symmetric matrix:
        // [1.0, 2.0, 3.0]
        // [2.0, 4.0, 5.0]
        // [3.0, 5.0, 6.0]
        // Packed upper triangle: [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
        double[] packed = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        DenseSymmetricMatrix matrix = new DenseSymmetricMatrix(3, packed);

        assertEquals(3, matrix.getDimension());
        assertEquals(3, matrix.rowCount());
        assertEquals(3, matrix.columnCount());

        assertEquals(1.0, matrix.get(0, 0));
        assertEquals(2.0, matrix.get(0, 1));
        assertEquals(3.0, matrix.get(0, 2));
        assertEquals(2.0, matrix.get(1, 0)); // Vérification de la symétrie
        assertEquals(4.0, matrix.get(1, 1));
        assertEquals(5.0, matrix.get(1, 2));
        assertEquals(3.0, matrix.get(2, 0)); // Vérification de la symétrie
        assertEquals(5.0, matrix.get(2, 1)); // Vérification de la symétrie
        assertEquals(6.0, matrix.get(2, 2));
    }

    @Test
    void testConstructorExceptions() {
        assertThrows(IllegalArgumentException.class, () -> new DenseSymmetricMatrix(0, new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> new DenseSymmetricMatrix(-1, new double[]{}));
        // Pour dim = 2, la taille attendue est 2 * 3 / 2 = 3
        assertThrows(IllegalArgumentException.class, () -> new DenseSymmetricMatrix(2, new double[]{1.0, 2.0}));
    }

    @Test
    void testAddSymmetricMatrix() {
        double[] packed1 = {1.0, 2.0, 3.0};
        double[] packed2 = {10.0, 20.0, 30.0};
        DenseSymmetricMatrix m1 = new DenseSymmetricMatrix(2, packed1);
        DenseSymmetricMatrix m2 = new DenseSymmetricMatrix(2, packed2);

        Matrix result = m1.add(m2);
        assertInstanceOf(DenseSymmetricMatrix.class, result);
        assertEquals(11.0, result.get(0, 0));
        assertEquals(22.0, result.get(0, 1));
        assertEquals(33.0, result.get(1, 1));
    }

    @Test
    void testSubtractSymmetricMatrix() {
        double[] packed1 = {10.0, 20.0, 30.0};
        double[] packed2 = {1.0, 2.0, 3.0};
        DenseSymmetricMatrix m1 = new DenseSymmetricMatrix(2, packed1);
        DenseSymmetricMatrix m2 = new DenseSymmetricMatrix(2, packed2);

        Matrix result = m1.subtract(m2);
        assertInstanceOf(DenseSymmetricMatrix.class, result);
        assertEquals(9.0, result.get(0, 0));
        assertEquals(18.0, result.get(0, 1));
        assertEquals(27.0, result.get(1, 1));
    }

    @Test
    void testMultiplyScalar() {
        double[] packed = {1.0, 2.0, 3.0};
        DenseSymmetricMatrix matrix = new DenseSymmetricMatrix(2, packed);

        Matrix result = matrix.multiply(2.0);
        assertInstanceOf(DenseSymmetricMatrix.class, result);
        assertEquals(2.0, result.get(0, 0));
        assertEquals(4.0, result.get(0, 1));
        assertEquals(6.0, result.get(1, 1));
    }

    @Test
    void testMultiplyMatrix() {
        double[] packed = {1.0, 2.0, 3.0}; // [[1, 2], [2, 3]]
        DenseSymmetricMatrix m1 = new DenseSymmetricMatrix(2, packed);
        DenseMatrix identity = new DenseMatrix(2, 2, new double[]{1.0, 0.0, 0.0, 1.0});

        Matrix result = m1.multiply(identity);
        assertInstanceOf(DenseMatrix.class, result);
        assertEquals(1.0, result.get(0, 0));
        assertEquals(2.0, result.get(0, 1));
        assertEquals(2.0, result.get(1, 0));
        assertEquals(3.0, result.get(1, 1));
    }

    @Test
    void testMultiplyVector() {
        // [1 2] * [1] = [1*1 + 2*2] = [5]
        // [2 3]   [2]   [2*1 + 3*2]   [8]
        double[] packed = {1.0, 2.0, 3.0};
        DenseSymmetricMatrix matrix = new DenseSymmetricMatrix(2, packed);
        Vector vector = new ArrayVector(new double[]{1.0, 2.0});

        Vector result = matrix.multiply(vector);
        assertEquals(5.0, result.getValue(0));
        assertEquals(8.0, result.getValue(1));
    }

    @Test
    void testGetRowAndColumn() {
        double[] packed = {1.0, 2.0, 3.0}; // Ligne 0 : [1, 2]
        DenseSymmetricMatrix matrix = new DenseSymmetricMatrix(2, packed);

        Vector row = matrix.getRow(0);
        assertEquals(1.0, row.getValue(0));
        assertEquals(2.0, row.getValue(1));

        Vector col = matrix.getColumn(0);
        assertEquals(1.0, col.getValue(0));
        assertEquals(2.0, col.getValue(1));
    }

    @Test
    void testBoundsChecking() {
        double[] packed = {1.0, 2.0, 3.0};
        DenseSymmetricMatrix matrix = new DenseSymmetricMatrix(2, packed);

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.getRow(2));
    }
}