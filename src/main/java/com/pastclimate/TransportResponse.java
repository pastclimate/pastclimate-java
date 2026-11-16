package com.pastclimate;

import java.util.Collections;
import java.util.Map;

/** What a transport returns: HTTP status, headers, body text. */
public final class TransportResponse {
    private final int status;
    private final Map<String, String> headers;
    private final String body;

    public TransportResponse(int status, Map<String, String> headers, String body) {
        this.status = status;
        this.headers = headers == null ? Collections.<String, String>emptyMap() : headers;
        this.body = body == null ? "" : body;
    }

    public int getStatus() {
        return status;
    }

    /** Header names may be in any case; the client lower-cases them. */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
