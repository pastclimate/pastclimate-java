package com.pastclimate;

import java.util.Map;

/**
 * No such place, nothing published for the request, or a place that cannot be served.
 * Codes: location_not_found, not_published, not_found, location_blocked.
 */
public class NotFoundException extends ApiException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
