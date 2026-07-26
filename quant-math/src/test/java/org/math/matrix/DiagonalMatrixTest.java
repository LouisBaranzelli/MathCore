package org.math.matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagonalMatrixTest {

    private static final double EPSILON = 1e-12;

    private boolean isClose(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @Nested
    @DisplayName("Initialisation et validations")
    class ConstructorTests {

        @Test
        @DisplayName("Levée d'exception sur données nulles ou vides")
        void shouldThrowOnInvalidData() {
            assertThrows(IllegalArgumentException.class, () -> new DiagonalMatrix(null));
            assertThrows(IllegalArgumentException.class, () -> new DiagonalMatrix(new double[]{}));
        }

        @Test
        @DisplayName("Vérification des dimensions et accès aux éléments extra-diagonaux")
        void shouldReturnZeroOffDiagonal() {
            DiagonalMatrix diag = new DiagonalMatrix(new double[]{3.0, 5.0, 7.0});

            assertEquals(3, diag.rowCount());
            assertEquals(3, diag.columnCount());

            assertTrue(isClose(diag.get(0, 0), 3.0));
            assertTrue(isClose(diag.get(1, 1), 5.0));
            assertTrue(isClose(diag.get(2, 2), 7.0));

            // Les éléments hors diagonale doivent être stricts 0.0
            assertTrue(isClose(diag.get(0, 1), 0.0));
            assertTrue(isClose(diag.get(2, 0), 0.0));
        }

        @Test
        @DisplayName("Isolation des données (copie défensive)")
        void shouldBeDefensiveAgainstArrayMutation() {
            double[] input = new double[]{1.0, 2.0};
            DiagonalMatrix diag = new DiagonalMatrix(input);
            input[0] = 999.0; // Mutation du tableau source

            assertTrue(isClose(diag.get(0, 0), 1.0));
        }
    }

    @Nested
    @DisplayName("Opérations algébriques et Fast-paths")
    class Operations {

        @Test
        @DisplayName("Addition entre deux matrices diagonales -> reste une DiagonalMatrix")
        void testAddDiagonalWithDiagonal() {
            Matrix d1 = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix d2 = new DiagonalMatrix(new double[]{3.0, 4.0});

            Matrix res = d1.add(d2);

            assertTrue(res instanceof DiagonalMatrix);
            assertTrue(isClose(res.get(0, 0), 4.0));
            assertTrue(isClose(res.get(1, 1), 6.0));
            assertTrue(isClose(res.get(0, 1), 0.0));
        }

        @Test
        @DisplayName("Addition avec une matrice dense -> retourne une DenseMatrix")
        void testAddDiagonalWithDense() {
            Matrix diag = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix dense = new DenseMatrix(new double[][]{{10.0, 20.0}, {30.0, 40.0}});

            Matrix res = diag.add(dense);

            assertTrue(res instanceof DenseMatrix);
            assertTrue(isClose(res.get(0, 0), 11.0));
            assertTrue(isClose(res.get(0, 1), 20.0));
            assertTrue(isClose(res.get(1, 0), 30.0));
            assertTrue(isClose(res.get(1, 1), 42.0));
        }

        @Test
        @DisplayName("Soustraction entre deux matrices diagonales")
        void testSubtractDiagonalWithDiagonal() {
            Matrix d1 = new DiagonalMatrix(new double[]{5.0, 8.0});
            Matrix d2 = new DiagonalMatrix(new double[]{2.0, 3.0});

            Matrix res = d1.subtract(d2);

            assertTrue(res instanceof DiagonalMatrix);
            assertTrue(isClose(res.get(0, 0), 3.0));
            assertTrue(isClose(res.get(1, 1), 5.0));
        }

        @Test
        @DisplayName("Multiplication par un scalaire")
        void testMultiplyScalar() {
            Matrix diag = new DiagonalMatrix(new double[]{2.0, -3.0});
            Matrix res = diag.multiply(2.5);

            assertTrue(res instanceof DiagonalMatrix);
            assertTrue(isClose(res.get(0, 0), 5.0));
            assertTrue(isClose(res.get(1, 1), -7.5));
        }

        @Test
        @DisplayName("Multiplication Diagonale x Diagonale -> O(N)")
        void testMultiplyDiagonalWithDiagonal() {
            Matrix d1 = new DiagonalMatrix(new double[]{2.0, -3.0});
            Matrix d2 = new DiagonalMatrix(new double[]{4.0, 5.0});

            Matrix res = d1.multiply(d2);

            assertTrue(res instanceof DiagonalMatrix);
            assertTrue(isClose(res.get(0, 0), 8.0));
            assertTrue(isClose(res.get(1, 1), -15.0));
        }

        @Test
        @DisplayName("Multiplication Diagonale x Dense -> O(N x C)")
        void testMultiplyDiagonalWithDense() {
            Matrix diag = new DiagonalMatrix(new double[]{2.0, 3.0});
            Matrix dense = new DenseMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

            Matrix res = diag.multiply(dense);

            assertTrue(res instanceof DenseMatrix);
            assertTrue(isClose(res.get(0, 0), 2.0));
            assertTrue(isClose(res.get(0, 1), 4.0));
            assertTrue(isClose(res.get(1, 0), 9.0));
            assertTrue(isClose(res.get(1, 1), 12.0));
        }

        @Test
        @DisplayName("Multiplication Diagonale x Vecteur -> O(N)")
        void testMultiplyVector() {
            Matrix diag = new DiagonalMatrix(new double[]{2.0, 3.0, 4.0});
            Vector v = new ArrayVector(new double[]{10.0, 20.0, 30.0});

            Vector res = diag.multiply(v);

            assertEquals(3, res.size());
            assertTrue(isClose(res.getValue(0), 20.0));
            assertTrue(isClose(res.getValue(1), 60.0));
            assertTrue(isClose(res.getValue(2), 120.0));
        }

        @Test
        @DisplayName("Transposée d'une matrice diagonale (doit être elle-même)")
        void testTranspose() {
            Matrix diag = new DiagonalMatrix(new double[]{1.0, 2.0, 3.0});
            Matrix transposed = diag.transpose();

            assertTrue(transposed.equals(diag));
        }

        @Test
        @DisplayName("Inversion directe O(N)")
        void testInverse() {
            DiagonalMatrix diag = new DiagonalMatrix(new double[]{2.0, 0.5, -4.0});
            DiagonalMatrix inv = diag.inverse();

            assertTrue(isClose(inv.get(0, 0), 0.5));
            assertTrue(isClose(inv.get(1, 1), 2.0));
            assertTrue(isClose(inv.get(2, 2), -0.25));
        }

        @Test
        @DisplayName("Levée d'exception lors de l'inversion avec un élément nul sur la diagonale")
        void shouldThrowOnInverseWithZeroElement() {
            DiagonalMatrix diag = new DiagonalMatrix(new double[]{2.0, 0.0, 4.0});
            assertThrows(ArithmeticException.class, diag::inverse);
        }
    }

    @Nested
    @DisplayName("Extraction et vérifications des erreurs de limites")
    class ExtractionsAndBoundaryTests {

        @Test
        @DisplayName("Extraction de ligne et de colonne")
        void testGetRowAndColumn() {
            DiagonalMatrix diag = new DiagonalMatrix(new double[]{4.0, 9.0});

            Vector row0 = diag.getRow(0);
            Vector col1 = diag.getColumn(1);

            assertTrue(isClose(row0.getValue(0), 4.0));
            assertTrue(isClose(row0.getValue(1), 0.0));

            assertTrue(isClose(col1.getValue(0), 0.0));
            assertTrue(isClose(col1.getValue(1), 9.0));
        }

        @Test
        @DisplayName("Levée d'exception sur des dimensions incompatibles")
        void shouldThrowOnDimensionMismatch() {
            Matrix d1 = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix d2 = new DiagonalMatrix(new double[]{1.0, 2.0, 3.0});
            Vector v = new ArrayVector(new double[]{1.0, 2.0, 3.0});

            assertThrows(IllegalArgumentException.class, () -> d1.add(d2));
            assertThrows(IllegalArgumentException.class, () -> d1.multiply(d2));
            assertThrows(IllegalArgumentException.class, () -> d1.multiply(v));
        }

        @Test
        @DisplayName("Levée d'exception sur index hors limites")
        void shouldThrowOnOutOfBoundsIndex() {
            DiagonalMatrix diag = new DiagonalMatrix(new double[]{1.0, 2.0});

            assertThrows(IndexOutOfBoundsException.class, () -> diag.get(-1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> diag.get(0, 2));
            assertThrows(IndexOutOfBoundsException.class, () -> diag.getRow(2));
            assertThrows(IndexOutOfBoundsException.class, () -> diag.getColumn(-1));
        }
    }

    @Nested
    @DisplayName("Tests d'égalité polymorphe (equals/hashCode)")
    class EqualityTests {

        @Test
        @DisplayName("Égalité entre deux DiagonalMatrix")
        void testEqualsWithDiagonal() {
            Matrix d1 = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix d2 = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix d3 = new DiagonalMatrix(new double[]{1.0, 3.0});

            assertTrue(d1.equals(d2));
            assertTrue(d1.hashCode() == d2.hashCode());
            assertFalse(d1.equals(d3));
        }

        @Test
        @DisplayName("Égalité polymorphe : DiagonalMatrix vs DenseMatrix")
        void testEqualsAcrossImplementations() {
            Matrix diag = new DiagonalMatrix(new double[]{1.0, 2.0});
            Matrix denseEquiv = new DenseMatrix(new double[][]{{1.0, 0.0}, {0.0, 2.0}});
            Matrix denseDiff = new DenseMatrix(new double[][]{{1.0, 0.1}, {0.0, 2.0}});

            assertTrue(diag.equals(denseEquiv));
            assertTrue(denseEquiv.equals(diag));
            assertFalse(diag.equals(denseDiff));
        }
    }
}