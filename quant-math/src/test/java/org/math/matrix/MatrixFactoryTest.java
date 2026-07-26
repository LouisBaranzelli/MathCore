package org.math.matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrixFactoryTest {

    private static final double EPSILON = 1e-12;

    private boolean isClose(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @Nested
    @DisplayName("Matrices spéciales et structurelles")
    class SpecialMatricesTests {

        @Test
        @DisplayName("identity() crée une DiagonalMatrix de taille NxN avec des 1.0 sur la diagonale")
        void testIdentity() {
            Matrix i = MatrixFactory.identity(3);

            assertTrue(i instanceof DiagonalMatrix);
            assertEquals(3, i.rowCount());
            assertEquals(3, i.columnCount());

            assertTrue(isClose(i.get(0, 0), 1.0));
            assertTrue(isClose(i.get(1, 1), 1.0));
            assertTrue(isClose(i.get(2, 2), 1.0));

            // Les éléments hors diagonale doivent être stricts 0.0
            assertTrue(isClose(i.get(0, 1), 0.0));
            assertTrue(isClose(i.get(1, 0), 0.0));
        }

        @Test
        @DisplayName("zeros() crée une matrice remplie uniquement de 0.0")
        void testZeros() {
            Matrix z = MatrixFactory.zeros(2, 3);

            assertEquals(2, z.rowCount());
            assertEquals(3, z.columnCount());

            for (int r = 0; r < z.rowCount(); r++) {
                for (int c = 0; c < z.columnCount(); c++) {
                    assertTrue(isClose(z.get(r, c), 0.0));
                }
            }
        }

        @Test
        @DisplayName("ones() et fill() remplissent correctement les matrices")
        void testOnesAndFill() {
            Matrix ones = MatrixFactory.ones(2, 2);
            assertTrue(isClose(ones.get(0, 0), 1.0));
            assertTrue(isClose(ones.get(1, 1), 1.0));

            Matrix filled = MatrixFactory.fill(2, 3, -4.5);
            assertEquals(2, filled.rowCount());
            assertEquals(3, filled.columnCount());

            for (int r = 0; r < filled.rowCount(); r++) {
                for (int c = 0; c < filled.columnCount(); c++) {
                    assertTrue(isClose(filled.get(r, c), -4.5));
                }
            }
        }

        @Test
        @DisplayName("diagonal() crée une DiagonalMatrix à partir d'un tableau")
        void testDiagonal() {
            double[] values = new double[]{2.0, 4.0, 6.0};
            Matrix diag = MatrixFactory.diagonal(values);

            assertTrue(diag instanceof DiagonalMatrix);
            assertEquals(3, diag.rowCount());
            assertTrue(isClose(diag.get(0, 0), 2.0));
            assertTrue(isClose(diag.get(1, 1), 4.0));
            assertTrue(isClose(diag.get(2, 2), 6.0));
            assertTrue(isClose(diag.get(0, 1), 0.0));
        }
    }

    @Nested
    @DisplayName("Génération aléatoire (Uniforme & Gaussienne)")
    class RandomMatricesTests {

        @Test
        @DisplayName("randomUniform() respecte les bornes fournies [min, max[")
        void testRandomUniformBounds() {
            double min = 10.0;
            double max = 20.0;
            Matrix rnd = MatrixFactory.randomUniform(10, 10, min, max);

            assertEquals(10, rnd.rowCount());
            assertEquals(10, rnd.columnCount());

            for (int r = 0; r < rnd.rowCount(); r++) {
                for (int c = 0; c < rnd.columnCount(); c++) {
                    double val = rnd.get(r, c);
                    assertTrue(val >= min && val < max, "Valeur hors bornes: " + val);
                }
            }
        }

        @Test
        @DisplayName("randomGaussian() génère des valeurs cohérentes pour N(mean, stdDev)")
        void testRandomGaussian() {
            Matrix gauss = MatrixFactory.randomGaussian(5, 5, 0.0, 1.0);

            assertEquals(5, gauss.rowCount());
            assertEquals(5, gauss.columnCount());

            // On vérifie qu'au moins deux éléments diffèrent (comportement stochastique non constant)
            assertFalse(isClose(gauss.get(0, 0), gauss.get(0, 1)));
        }

        @Test
        @DisplayName("Reproductibilité : La même graine (seed) produit des matrices identiques")
        void testSeededRandom() {
            long seed = 42L;
            Matrix m1 = MatrixFactory.randomUniform(4, 4, -1.0, 1.0, seed);
            Matrix m2 = MatrixFactory.randomUniform(4, 4, -1.0, 1.0, seed);

            assertTrue(m1.equals(m2));
        }
    }

    @Nested
    @DisplayName("Validations des arguments et cas d'erreurs")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Levée d'exception pour des dimensions négatives ou nulles")
        void shouldThrowOnInvalidDimensions() {
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.identity(0));
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.identity(-2));

            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.zeros(0, 5));
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.zeros(5, -1));

            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.fill(-1, -1, 3.0));
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.random(0, 2));
        }

        @Test
        @DisplayName("Levée d'exception si min >= max dans la loi uniforme")
        void shouldThrowOnInvalidUniformBounds() {
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.randomUniform(2, 2, 5.0, 2.0));
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.randomUniform(2, 2, 5.0, 5.0));
        }

        @Test
        @DisplayName("Levée d'exception si l'écart-type est négatif ou nul")
        void shouldThrowOnInvalidStdDev() {
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.randomGaussian(2, 2, 0.0, 0.0));
            assertThrows(IllegalArgumentException.class, () -> MatrixFactory.randomGaussian(2, 2, 0.0, -1.5));
        }
    }
}