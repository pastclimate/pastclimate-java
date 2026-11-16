package com.pastclimate;

import java.util.Map;

/**
 * No key, a bad key, a revoked key, or an account that cannot make calls.
 * Codes: missing_key, invalid_key, key_revoked, email_not_verified, account_suspended.
 */
public class AuthenticationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
