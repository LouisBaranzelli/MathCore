package org.math.random;

import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomVectorFactoryTest {
    @Test
    void testShuffleImmutability() {
        // Given
        double[] initialValues = {10.0, 20.0, 30.0};
        Vector original = new ArrayVector(initialValues);
        Random random = new Random(123);
        Vector shuffled = RandomVectorFactory.shuffle(original, random);
        assertArrayEquals(initialValues, original.toArray(), 1e-9);
        assertEquals(initialValues[0]+initialValues[1] + initialValues[2], shuffled.getValue(0) + shuffled.getValue(1) + shuffled.getValue(2), 1e-9);
        assertNotSame(original, shuffled);
    }

    @Test
    void testShuffleWithSingleElement() {
        Vector original = new ArrayVector(42.0);
        Random random = new Random(42);
        Vector shuffled = RandomVectorFactory.shuffle(original, random);
        assertNotNull(shuffled);
        assertEquals(1, shuffled.size());
        assertEquals(42.0, shuffled.getValue(0), 1e-9);
    }

}