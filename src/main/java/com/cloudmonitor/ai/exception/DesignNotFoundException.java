package com.cloudmonitor.ai.exception;

/**
 * Exception thrown when a design is not found.
 */
public class DesignNotFoundException extends RuntimeException {

    public DesignNotFoundException(String message) {
        super(message);
    }

    public DesignNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
