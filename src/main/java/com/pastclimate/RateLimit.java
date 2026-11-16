package com.pastclimate;

import java.util.Map;
import java.util.Objects;

/**
 * The per-minute request limit as the server reported it. Any value is null when the response did not
 * carry that header (some refusals do not).
 */
public final class RateLimit {
    private final Integer limit;
    private final Integer remaining;
    private final Integer reset;

    public RateLimit(Integer limit, Integer remaining, Integer reset) {
        this.limit = limit;
        this.remaining = remaining;
        this.reset = reset;
    }

    static RateLimit fromHeaders(Map<String, String> headers) {
        return new RateLimit(
                Headers.smallInteger(headers, "ratelimit-limit"),
                Headers.smallInteger(headers, "ratelimit-remaining"),
                Headers.smallInteger(headers, "ratelimit-reset"));
    }

    /** Requests allowed per minute. */
    public Integer getLimit() {
        return limit;
    }

    /** Requests left in the current minute. */
    public Integer getRemaining() {
        return remaining;
    }

    /** Seconds until the oldest request leaves the minute window. */
    public Integer getReset() {
        return reset;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RateLimit)) {
            return false;
        }
        RateLimit that = (RateLimit) other;
        return Objects.equals(limit, that.limit)
                && Objects.equals(remaining, that.remaining)
                && Objects.equals(reset, that.reset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, remaining, reset);
    }

    @Override
    public String toString() {
        return "RateLimit{limit=" + limit + ", remaining=" + remaining + ", reset=" + reset + "}";
    }
}
