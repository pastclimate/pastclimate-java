package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;

/**
 * Credit balance and usage.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MeCredits {
    @SerializedName("one_time_limit")
    private Long oneTimeLimit;

    @SerializedName("one_time_remaining")
    private Long oneTimeRemaining;

    @SerializedName("used_all_time")
    private Long usedAllTime;

    @SerializedName("monthly_limit")
    private Long monthlyLimit;

    @SerializedName("used_this_month")
    private Long usedThisMonth;

    @SerializedName("remaining_this_month")
    private Long remainingThisMonth;

    @SerializedName("month_resets_at")
    private Instant monthResetsAt;

    @SerializedName("daily_cap")
    private Long dailyCap;

    @SerializedName("used_today")
    private Long usedToday;

    @SerializedName("remaining_today")
    private Long remainingToday;

    @SerializedName("day_resets_at")
    private Instant dayResetsAt;

    @SerializedName("topup_balance")
    private Long topupBalance;

    @SerializedName("topup_used")
    private Long topupUsed;

    /** One-time credits for the life of the account. They never refill. null when your plan has none. */
    public Long getOneTimeLimit() {
        return oneTimeLimit;
    }

    /** One-time credits left. null when your plan has none. */
    public Long getOneTimeRemaining() {
        return oneTimeRemaining;
    }

    /** Credits used since the account was created, on any plan. */
    public Long getUsedAllTime() {
        return usedAllTime;
    }

    /** Credits in your plan each month. null when your plan has no monthly credits. */
    public Long getMonthlyLimit() {
        return monthlyLimit;
    }

    /** Credits used this month (UTC). */
    public Long getUsedThisMonth() {
        return usedThisMonth;
    }

    /** Credits left this month. null when your plan has no monthly credits. */
    public Long getRemainingThisMonth() {
        return remainingThisMonth;
    }

    /** When the monthly allowance next renews (UTC). null when your plan has no monthly allowance. */
    public Instant getMonthResetsAt() {
        return monthResetsAt;
    }

    /** Most credits you can use in a day. null when there is no daily credit cap. */
    public Long getDailyCap() {
        return dailyCap;
    }

    /** Credits used today (UTC). */
    public Long getUsedToday() {
        return usedToday;
    }

    /** Credits left today. null when there is no daily credit cap. */
    public Long getRemainingToday() {
        return remainingToday;
    }

    /** When the daily count resets. */
    public Instant getDayResetsAt() {
        return dayResetsAt;
    }

    /** Bought credits left. They never expire and never reset. Calls spend your plan's credits first, then these. */
    public Long getTopupBalance() {
        return topupBalance;
    }

    /** Bought credits spent so far. */
    public Long getTopupUsed() {
        return topupUsed;
    }

    @Override
    public String toString() {
        return "MeCredits{" + "oneTimeLimit=" + oneTimeLimit + ", oneTimeRemaining=" + oneTimeRemaining + ", usedAllTime=" + usedAllTime + ", monthlyLimit=" + monthlyLimit + ", usedThisMonth=" + usedThisMonth + ", remainingThisMonth=" + remainingThisMonth + ", monthResetsAt=" + monthResetsAt + ", dailyCap=" + dailyCap + ", usedToday=" + usedToday + ", remainingToday=" + remainingToday + ", dayResetsAt=" + dayResetsAt + ", topupBalance=" + topupBalance + ", topupUsed=" + topupUsed + "}";
    }
}
