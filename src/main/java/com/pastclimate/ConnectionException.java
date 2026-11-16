package com.pastclimate;

/** The request never produced an HTTP answer: DNS, TLS, connection reset or timeout. */
public class ConnectionException extends PastClimateException {
    private static final long serialVersionUID = 1L;

    private final boolean timeout;

    public ConnectionException(String message) {
        this(message, false, null);
    }

    public ConnectionException(String message, boolean timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    /** True when the call timed out. */
    public boolean isTimeout() {
        return timeout;
    }
}
