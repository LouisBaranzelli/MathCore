package org.example.common.exception;

import org.example.common.Shape;

public class ShapeException extends RuntimeException {

    public ShapeException(Shape left, Shape right) {
        super("Shapes doesn't match: " + left.toString() + " and " + right.toString());
    }
}
