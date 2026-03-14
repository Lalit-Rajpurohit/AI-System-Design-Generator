package com.cloudmonitor.ai.exception;

/**
 * Exception thrown when LLM operations fail.
 */
public class LLMException extends RuntimeException {

    private final String provider;
    private final boolean retryable;
    private final Integer statusCode;

    public LLMException(String message) {
        super(message);
        this.provider = null;
        this.retryable = false;
        this.statusCode = null;
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
        this.retryable = false;
        this.statusCode = null;
    }

    public LLMException(String message, String provider, boolean retryable) {
        super(message);
        this.provider = provider;
        this.retryable = retryable;
        this.statusCode = null;
    }

    public LLMException(String message, String provider, boolean retryable, Integer statusCode) {
        super(message);
        this.provider = provider;
        this.retryable = retryable;
        this.statusCode = statusCode;
    }

    public LLMException(String message, Throwable cause, String provider, boolean retryable) {
        super(message, cause);
        this.provider = provider;
        this.retryable = retryable;
        this.statusCode = null;
    }

    public String getProvider() {
        return provider;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.format("LLMException{provider='%s', retryable=%s, statusCode=%d, message='%s'}",
                provider, retryable, statusCode, getMessage());
    }
}
