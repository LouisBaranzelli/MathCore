package org.math.matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DenseMatrixTest {

    private static final double EPSILON = 1e-12;

    private boolean isClose(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @Nested
    @DisplayName("Constructeurs et cas d'erreurs d'initialisation")
    class ConstructorTests {

        @Test
        @DisplayName("Validation des dimensions positives")
        void shouldThrowOnInvalidDimensions() {
            assertThrows(IllegalArgumentException.class, () -> new DenseMatrix(0, 2, new double[]{1, 2}));
            assertThrows(IllegalArgumentException.class, () -> new DenseMatrix(2, -1, new double[]{1, 2}));
            assertThrows(IllegalArgumentException.class, () -> new DenseMatrix(null));
        }

        @Test
        @DisplayName("Validation de la taille du tableau de primitives")
        void shouldThrowOnArrayLengthMismatch() {
            assertThrows(IllegalArgumentException.class, () -> new DenseMatrix(2, 2, new double[]{1, 2, 3}));
        }

        @Test
        @DisplayName("Validation de la régularité des lignes du tableau 2D")
        void shouldThrowOnIrregular2DArray() {
            double[][] irregular = new double[][]{{1, 2}, {3}};
            assertThrows(IllegalArgumentException.class, () -> new DenseMatrix(irregular));
        }
    }

    @Nested
    @DisplayName("Opérations d'addition, soustraction et scalaires")
    class BasicOperations {

        @Test
        @DisplayName("Addition de deux matrices")
        void testAdd() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});
            Matrix b = new DenseMatrix(new double[][]{{5, 6}, {7, 8}});
            Matrix res = a.add(b);

            assertTrue(isClose(res.get(0, 0), 6.0));
            assertTrue(isClose(res.get(0, 1), 8.0));
            assertTrue(isClose(res.get(1, 0), 10.0));
            assertTrue(isClose(res.get(1, 1), 12.0));
        }

        @Test
        @DisplayName("Soustraction de deux matrices")
        void testSubtract() {
            Matrix a = new DenseMatrix(new double[][]{{5, 6}, {7, 8}});
            Matrix b = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});
            Matrix res = a.subtract(b);

            assertTrue(isClose(res.get(0, 0), 4.0));
            assertTrue(isClose(res.get(0, 1), 4.0));
            assertTrue(isClose(res.get(1, 0), 4.0));
            assertTrue(isClose(res.get(1, 1), 4.0));
        }

        @Test
        @DisplayName("Multiplication par un scalaire")
        void testMultiplyScalar() {
            Matrix a = new DenseMatrix(new double[][]{{1, -2}, {3, 0}});
            Matrix res = a.multiply(2.5);

            assertTrue(isClose(res.get(0, 0), 2.5));
            assertTrue(isClose(res.get(0, 1), -5.0));
            assertTrue(isClose(res.get(1, 0), 7.5));
            assertTrue(isClose(res.get(1, 1), 0.0));
        }

        @Test
        @DisplayName("Incompatibilité de dimensions sur l'addition")
        void shouldThrowOnAddDimensionMismatch() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});
            Matrix b = new DenseMatrix(new double[][]{{1, 2, 3}, {4, 5, 6}});

            assertThrows(IllegalArgumentException.class, () -> a.add(b));
        }
    }

    @Nested
    @DisplayName("Produits matriciels et vectoriels")
    class Multiplications {

        @Test
        @DisplayName("Produit Matrice x Matrice (2x3 * 3x2)")
        void testMatrixMultiplication() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2, 3}, {4, 5, 6}});
            Matrix b = new DenseMatrix(new double[][]{{7, 8}, {9, 1}, {2, 3}});
            Matrix res = a.multiply(b);

            assertTrue(res.rowCount() == 2);
            assertTrue(res.columnCount() == 2);
            assertTrue(isClose(res.get(0, 0), 31.0));
            assertTrue(isClose(res.get(0, 1), 19.0));
            assertTrue(isClose(res.get(1, 0), 85.0));
            assertTrue(isClose(res.get(1, 1), 55.0));
        }

        @Test
        @DisplayName("Produit Matrice x Vecteur")
        void testMatrixVectorMultiplication() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});
            Vector v = new ArrayVector(new double[]{5, 6});
            Vector res = a.multiply(v);

            assertTrue(res.size() == 2);
            assertTrue(isClose(res.getValue(0), 17.0));
            assertTrue(isClose(res.getValue(1), 39.0));
        }

        @Test
        @DisplayName("Incompatibilité de dimensions sur la multiplication")
        void shouldThrowOnMultiplyDimensionMismatch() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});
            Matrix b = new DenseMatrix(new double[][]{{1, 2, 3}});

            assertThrows(IllegalArgumentException.class, () -> a.multiply(b));
        }
    }

    @Nested
    @DisplayName("Transposition et extraction de lignes/colonnes")
    class ExtractionsAndTranspose {

        @Test
        @DisplayName("Transposition d'une matrice rectangulaire")
        void testTranspose() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2, 3}, {4, 5, 6}});
            Matrix t = a.transpose();

            assertTrue(t.rowCount() == 3);
            assertTrue(t.columnCount() == 2);
            assertTrue(isClose(t.get(0, 0), 1.0));
            assertTrue(isClose(t.get(0, 1), 4.0));
            assertTrue(isClose(t.get(2, 1), 6.0));
        }

        @Test
        @DisplayName("Extraction de ligne et colonne")
        void testGetRowAndColumn() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});

            Vector row1 = a.getRow(1);
            Vector col0 = a.getColumn(0);

            assertTrue(isClose(row1.getValue(0), 3.0));
            assertTrue(isClose(row1.getValue(1), 4.0));
            assertTrue(isClose(col0.getValue(0), 1.0));
            assertTrue(isClose(col0.getValue(1), 3.0));
        }

        @Test
        @DisplayName("Levée d'exception sur index hors limites")
        void shouldThrowOnOutOfBoundsIndex() {
            Matrix a = new DenseMatrix(new double[][]{{1, 2}, {3, 4}});

            assertThrows(IndexOutOfBoundsException.class, () -> a.get(2, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> a.getRow(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> a.getColumn(5));
        }
    }

    @Nested
    @DisplayName("Egalité stricte (equals/hashCode)")
    class EqualityTests {

        @Test
        @DisplayName("Verification du contrat equals et hashCode")
        void testEqualsAndHashCode() {
            Matrix a = new DenseMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
            Matrix b = new DenseMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
            Matrix c = new DenseMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.000000001}});

            assertTrue(a.equals(a));
            assertTrue(a.equals(b));
            assertTrue(b.equals(a));
            assertTrue(a.hashCode() == b.hashCode());

            assertFalse(a.equals(c));
            assertFalse(a.equals(null));
            assertFalse(a.equals("Not a matrix"));
        }
    }
}