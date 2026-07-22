package org.series;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.Vector;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.SliceDoubleTimeSerie;

import static org.junit.jupiter.api.Assertions.*;

class SliceDoubleTimeSerieTest {

    private DoubleTimeSerie sourceSerie;

    @BeforeEach
    void setUp() {
        // Remplacement de ImmutableDoubleTimeSerie par un Stub anonyme isolé de toute dépendance.
        // On simule une série de taille 5 (indices de 0 à 4).
        sourceSerie = new DoubleTimeSerie() {
            private final long[] timestamps = {1000L, 2000L, 3000L, 4000L, 5000L};
            private final double[] values = {10.0, 20.0, 30.0, 40.0, 50.0};

            @Override
            public int size() {
                return 5;
            }

            @Override
            public double getValue(int index) {
                return values[index];
            }

            @Override
            public long getTimestamp(int index) {
                return timestamps[index];
            }

            @Override
            public Vector toVector() {
                throw new UnsupportedOperationException("Non nécessaire pour ce stub");
            }
        };
    }

    @Test
    @DisplayName("Devrait créer une tranche valide au milieu de la série")
    void testSliceCreationMiddle() {
        // Tranche de start=1 (inclus) à end=4 (exclu) -> indices sources 1, 2, 3 (valeurs : 20, 30, 40)
        SliceDoubleTimeSerie slice = new SliceDoubleTimeSerie(sourceSerie, 1, 4);

        assertEquals(3, slice.size(), "La taille de la tranche doit être de end - start = 3");

        // Assertions sur les valeurs (indices relatifs à la tranche)
        assertEquals(20.0, slice.getValue(0));
        assertEquals(30.0, slice.getValue(1));
        assertEquals(40.0, slice.getValue(2));

        // Assertions sur les timestamps
        assertEquals(2000L, slice.getTimestamp(0));
        assertEquals(3000L, slice.getTimestamp(1));
        assertEquals(4000L, slice.getTimestamp(2));
    }

    @Test
    @DisplayName("Devrait créer une tranche couvrant l'intégralité de la série")
    void testSliceFull() {
        SliceDoubleTimeSerie slice = new SliceDoubleTimeSerie(sourceSerie, 0, 5);

        assertEquals(5, slice.size());
        assertEquals(10.0, slice.getValue(0));
        assertEquals(50.0, slice.getValue(4));
    }

    @Test
    @DisplayName("Devrait rejeter la création si la source est null")
    void testInvalidSourceNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new SliceDoubleTimeSerie(null, 0, 2)
        );
    }

    @Test
    @DisplayName("Devrait rejeter la création si start est négatif")
    void testInvalidStartNegative() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                new SliceDoubleTimeSerie(sourceSerie, -1, 3)
        );
    }

    @Test
    @DisplayName("Devrait rejeter la création si end dépasse la taille de la source")
    void testInvalidEndOverBounds() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                new SliceDoubleTimeSerie(sourceSerie, 2, 6) // end=6 > source.size()=5
        );
    }

    @Test
    @DisplayName("Devrait rejeter la création si start >= end (longueur nulle ou négative)")
    void testInvalidZeroOrNegativeLength() {
        // Cas start == end (longueur = 0 -> condition 'length <= 0' déclenchée)
        assertThrows(IndexOutOfBoundsException.class, () ->
                new SliceDoubleTimeSerie(sourceSerie, 2, 2)
        );

        // Cas start > end (longueur négative)
        assertThrows(IndexOutOfBoundsException.class, () ->
                new SliceDoubleTimeSerie(sourceSerie, 3, 2)
        );
    }

    @Test
    @DisplayName("getValue et getTimestamp devraient lever IndexOutOfBoundsException si l'index demandé est hors limites")
    void testGetAccessOutOfBounds() {
        SliceDoubleTimeSerie slice = new SliceDoubleTimeSerie(sourceSerie, 1, 3); // indices 1 et 2 (taille = 2)

        // Limite basse relative à la tranche
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getValue(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getTimestamp(-1));

        // Limite haute relative à la tranche (index max valide = size - 1 = 1)
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getValue(2));
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getTimestamp(2));
    }

    @Test
    @DisplayName("Devrait convertir correctement la tranche en un Vector")
    void testToVector() {
        // Tranche de start=2 à end=5 (indices sources 2, 3, 4 -> valeurs : 30.0, 40.0, 50.0)
        SliceDoubleTimeSerie slice = new SliceDoubleTimeSerie(sourceSerie, 2, 5);
        Vector resultVector = slice.toVector();

        assertNotNull(resultVector, "Le vecteur retourné ne doit pas être null");
        assertEquals(3, resultVector.size(), "Le vecteur doit avoir la même taille que la tranche");

        // Vérification des éléments extraits
        assertEquals(30.0, resultVector.getValue(0));
        assertEquals(40.0, resultVector.getValue(1));
        assertEquals(50.0, resultVector.getValue(2));
    }
}