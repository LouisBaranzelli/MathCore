package org.math.matrix;

public class NotPositiveDefiniteMatrixException extends RuntimeException {

    public NotPositiveDefiniteMatrixException(String message) {
        super(message);
    }

    public NotPositiveDefiniteMatrixException(String message, Throwable cause) {
        super(message, cause);
    }
}