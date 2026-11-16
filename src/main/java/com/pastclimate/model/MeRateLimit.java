package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Per-minute request limit.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MeRateLimit {
    @SerializedName("per_minute")
    private Integer perMinute;

    @SerializedName("remaining")
    private Integer remaining;

    /** Requests allowed per minute. */
    public Integer getPerMinute() {
        return perMinute;
    }

    /** Requests left in the current minute. */
    public Integer getRemaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "MeRateLimit{" + "perMinute=" + perMinute + ", remaining=" + remaining + "}";
    }
}
