package com.pastclimate;

import java.util.Map;

/**
 * The API answered with an error. Catch the subclass for the kind of failure, or branch on
 * {@link #getCode()}.
 *
 * <p>When the server did not send the API's error body (for example a proxy 502), the code is
 * {@code http_<status>} and {@link #getDocsUrl()} is null.
 */
public class ApiException extends PastClimateException {
    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;
    private final String docsUrl;
    private final transient Map<String, String> headers;
    private final transient RateLimit rateLimit;
    private final Long retryAfter;
    private final String upgradeUrl;
    private final String body;

    public ApiException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message);
        this.status = status;
        this.code = code;
        this.docsUrl = docsUrl;
        this.headers = Headers.normalize(headers);
        this.rateLimit = RateLimit.fromHeaders(this.headers);
        this.retryAfter = Headers.retryAfter(this.headers);
        this.upgradeUrl = this.headers.get("x-upgrade-url");
        this.body = body;
    }

    /** The HTTP status. */
    public int getStatus() {
        return status;
    }

    /** The API's stable error code, for example {@code location_not_found}. */
    public String getCode() {
        return code;
    }

    /** The docs page for the code, or null. */
    public String getDocsUrl() {
        return docsUrl;
    }

    /** Response headers, with lower-case names. */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /** The per-minute request limit as the server reported it. */
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    /** Seconds the server asked you to wait, or null (no Retry-After header). */
    public Long getRetryAfter() {
        return retryAfter;
    }

    /** Where to upgrade, when the server said (X-Upgrade-URL), else null. */
    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    /** The raw response body. */
    public String getBody() {
        return body;
    }
}
