package org.math.vector;

import org.math.common.Shape;
import org.math.common.exception.ShapeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorTest {

    @Test
    void shouldCreateVector() {
        Vector vector = new Vector(1.0, 2.0, 3.0);

        assertEquals(3, vector.getSize());
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, vector.getValues());
        assertEquals(new Shape(3, 1), vector.getShape());
    }

    @Test
    void shouldRejectNullValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vector((double[]) null));
    }

    @Test
    void shouldRejectEmptyValues() {
        assertThrows(IllegalArgumentException.class,
                Vector::new);
    }

    @Test
    void shouldReturnCopyOfValues() {
        Vector vector = new Vector(1.0, 2.0, 3.0);

        double[] values = vector.getValues();
        values[0] = 99.0;

        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, vector.getValues());
    }

    @Test
    void shouldAddVectors() {
        Vector a = new Vector(1, 2, 3);
        Vector b = new Vector(4, 5, 6);

        Vector result = a.add(b);

        assertEquals(new Vector(5, 7, 9), result);
    }

    @Test
    void shouldSubtractVectors() {
        Vector a = new Vector(5, 7, 9);
        Vector b = new Vector(1, 2, 3);

        Vector result = a.minus(b);

        assertEquals(new Vector(4, 5, 6), result);
    }

    @Test
    void shouldComputeDotProduct() {
        Vector a = new Vector(1, 2, 3);
        Vector b = new Vector(4, 5, 6);

        assertEquals(32.0, a.dot(b));
    }

    @Test
    void shouldRejectAdditionWithDifferentShapes() {
        Vector a = new Vector(1, 2);
        Vector b = new Vector(1, 2, 3);

        assertThrows(ShapeException.class, () -> a.add(b));
    }

    @Test
    void shouldRejectSubtractionWithDifferentShapes() {
        Vector a = new Vector(1, 2);
        Vector b = new Vector(1, 2, 3);

        assertThrows(ShapeException.class, () -> a.minus(b));
    }

    @Test
    void shouldRejectDotProductWithDifferentShapes() {
        Vector a = new Vector(1, 2);
        Vector b = new Vector(1, 2, 3);

        assertThrows(ShapeException.class, () -> a.dot(b));
    }

    @Test
    void shouldComputeSum() {
        Vector vector = new Vector(1, 2, 3, 4);

        assertEquals(10.0, vector.sum());
    }

    @Test
    void shouldComputeMean() {
        Vector vector = new Vector(2, 4, 6, 8);

        assertEquals(5.0, vector.mean());
    }

    @Test
    void shouldComputeNorm() {
        Vector vector = new Vector(3, 4);

        assertEquals(5.0, vector.norm(), 1e-9);
    }

    @Test
    void shouldBeEqual() {
        Vector a = new Vector(1, 2, 3);
        Vector b = new Vector(1, 2, 3);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqual() {
        Vector a = new Vector(1, 2, 3);
        Vector b = new Vector(3, 2, 1);

        assertNotEquals(a, b);
    }

    @Test
    void shouldNotEqualOtherObject() {
        Vector vector = new Vector(1, 2, 3);

        assertNotEquals(vector, "vector");
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        Vector vector = new Vector(1, 2, 3);

        assertEquals("[1.0, 2.0, 3.0]", vector.toString());
    }
}