package com.pastclimate;

import java.util.Map;

/**
 * Something went wrong on the server, or the API is unavailable.
 * Codes: internal_error, api_disabled, metering_unavailable. Also thrown (code invalid_response) when a
 * success answer cannot be read.
 */
public class ServerException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ServerException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
