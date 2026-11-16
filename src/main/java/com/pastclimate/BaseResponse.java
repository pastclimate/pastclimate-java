package com.pastclimate;

import com.pastclimate.model.Meta;
import java.util.Map;

/** What every answer has in common: status, headers, rate limit and credit numbers. */
public abstract class BaseResponse {
    private final int status;
    private final Map<String, String> headers;
    private final RateLimit rateLimit;
    private final Meta meta;

    BaseResponse(int status, Map<String, String> headers, Meta meta) {
        this.status = status;
        this.headers = headers;
        this.rateLimit = RateLimit.fromHeaders(headers);
        this.meta = meta;
    }

    /** The HTTP status (a 2xx). */
    public int getStatus() {
        return status;
    }

    /** Response headers, with lower-case names. */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /** The per-minute request limit. */
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    /** The response metadata, or null for a CSV answer. */
    public Meta getMeta() {
        return meta;
    }

    /** Credits this call used (meta.credits, else the X-Credits-Used header), or null. */
    public Long getCreditsUsed() {
        if (meta != null && meta.getCredits() != null) {
            return meta.getCredits();
        }
        return Headers.integer(headers, "x-credits-used");
    }

    /** Credits you can still spend (meta.credits_remaining, else the X-Credits-Remaining header), or null. */
    public Long getCreditsRemaining() {
        if (meta != null && meta.getCreditsRemaining() != null) {
            return meta.getCreditsRemaining();
        }
        return Headers.integer(headers, "x-credits-remaining");
    }
}
