package org.series.timegrid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IrregularTimeGridTest {

    @Test
    @DisplayName("Should create grid and return correct size and timestamps when values are valid")
    void shouldCreateGridWhenValid() {
        // GIVEN
        long[] validTimestamps = new long[]{1000L, 2000L, 3000L};

        // WHEN
        IrregularTimeGrid grid = new IrregularTimeGrid(validTimestamps);

        // THEN
        assertEquals(3, grid.size());
        assertEquals(1000L, grid.getTimeStamp(0));
        assertEquals(2000L, grid.getTimeStamp(1));
        assertEquals(3000L, grid.getTimeStamp(2));
    }

    @Test
    @DisplayName("Should protect internal data against external mutations (Immutability check)")
    void shouldBeImmutable() {
        // GIVEN
        long[] original = new long[]{1000L, 2000L};
        IrregularTimeGrid grid = new IrregularTimeGrid(original);

        // WHEN : On modifie le tableau d'origine après la création
        original[0] = 9999L;

        // THEN : La grille interne ne doit pas avoir bougé grâce au .clone()
        assertEquals(1000L, grid.getTimeStamp(0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when timestamps array is null")
    void shouldThrowExceptionWhenNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new IrregularTimeGrid(null)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when timestamps array is empty")
    void shouldThrowExceptionWhenEmpty() {
     assertThrows(IllegalArgumentException.class, () ->
                new IrregularTimeGrid(new long[0])
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when timestamps are decreasing or equal")
    void shouldThrowExceptionWhenNotStrictlyIncreasing() {
        // Cas 1 : Décroissant
        long[] decreasing = new long[]{1000L, 900L, 1100L};
        assertThrows(IllegalArgumentException.class, () ->
                new IrregularTimeGrid(decreasing)
        );

        // Cas 2 : Doublon / Égal
        long[] duplicates = new long[]{1000L, 2000L, 2000L, 3000L};
        assertThrows(IllegalArgumentException.class, () ->
                new IrregularTimeGrid(duplicates)
        );
    }
}