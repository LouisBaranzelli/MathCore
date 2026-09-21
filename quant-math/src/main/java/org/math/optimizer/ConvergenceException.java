package org.math.optimizer;

public class ConvergenceException extends RuntimeException {
    public ConvergenceException(String message) {
        super(message);
    }

    public ConvergenceException(String message, Throwable cause) {
        super(message, cause);
    }
}