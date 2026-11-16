package com.pastclimate;

import java.util.Map;

/**
 * The request is not valid. Fix it; retrying will not help.
 * Codes: invalid_request, range_too_large, key_in_query, cors_not_supported, method_not_allowed.
 */
public class InvalidRequestException extends ApiException {
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
