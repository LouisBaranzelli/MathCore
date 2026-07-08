package org.math.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShapeTest {

    @Test
    void shouldCreateShapeWithValidDimensions() {
        Shape shape = new Shape(3, 4, 5);
        assertEquals(3, shape.countDimensions());
        assertEquals(3, shape.getDimension(0));
        assertEquals(4, shape.getDimension(1));
        assertEquals(5, shape.getDimension(2));
    }

    @Test
    void shouldRejectNullDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shape((int[]) null));
    }

    @Test
    void shouldRejectEmptyDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shape());
    }

    @Test
    void shouldRejectNegativeDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shape(3, -1, 5));
    }


    @Test
    void shouldReturnCopyOfDimensions() {
        Shape shape = new Shape(2, 3, 4);

        int[] dimensions = shape.getDimensions();
        dimensions[0] = 99;

        assertEquals(2, shape.getDimension(0));
    }

    @Test
    void shouldBeEqualWhenDimensionsAreSame() {
        Shape shape1 = new Shape(2, 3, 4);
        Shape shape2 = new Shape(2, 3, 4);

        assertEquals(shape1, shape2);
    }

    @Test
    void shouldNotBeEqualWhenDimensionsAreDifferent() {
        Shape shape1 = new Shape(2, 3, 4);
        Shape shape2 = new Shape(2, 4, 3);

        assertNotEquals(shape1, shape2);
    }

    @Test
    void shouldNotBeEqualWithAnotherType() {
        Shape shape = new Shape(2, 3);

        assertNotEquals(shape, "2x3");
    }

    @Test
    void shouldGenerateCorrectStringRepresentation() {
        Shape shape = new Shape(2, 3, 4);

        assertEquals("(2x3x4)", shape.toString());
    }
}