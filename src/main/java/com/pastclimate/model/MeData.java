package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Your plan, credit balance and limits.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MeData {
    @SerializedName("plan")
    private MePlan plan;

    @SerializedName("key")
    private MeKey key;

    @SerializedName("credits")
    private MeCredits credits;

    @SerializedName("rate_limit")
    private MeRateLimit rateLimit;

    public MePlan getPlan() {
        return plan;
    }

    public MeKey getKey() {
        return key;
    }

    public MeCredits getCredits() {
        return credits;
    }

    public MeRateLimit getRateLimit() {
        return rateLimit;
    }

    @Override
    public String toString() {
        return "MeData{" + "plan=" + plan + ", key=" + key + ", credits=" + credits + ", rateLimit=" + rateLimit + "}";
    }
}
