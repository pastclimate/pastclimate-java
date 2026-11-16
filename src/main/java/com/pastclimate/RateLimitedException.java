package com.pastclimate;

import java.util.Map;

/**
 * Too many requests (per minute or per day). {@link #getRetryAfter()} says how long to wait.
 * Codes: rate_limited, daily_request_cap_reached.
 */
public class RateLimitedException extends ApiException {
    private static final long serialVersionUID = 1L;

    public RateLimitedException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
