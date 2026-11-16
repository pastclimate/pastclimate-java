package com.pastclimate;

/** Base class of everything this SDK throws on purpose. Unchecked. */
public class PastClimateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PastClimateException(String message) {
        super(message);
    }

    public PastClimateException(String message, Throwable cause) {
        super(message, cause);
    }
}
