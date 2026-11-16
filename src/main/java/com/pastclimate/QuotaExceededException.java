package com.pastclimate;

import java.util.Map;

/**
 * Credits are used up. Code: quota_exceeded. On a plan whose credits are one-time there is no
 * {@link #getRetryAfter()}, because waiting does not help.
 */
public class QuotaExceededException extends ApiException {
    private static final long serialVersionUID = 1L;

    public QuotaExceededException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
