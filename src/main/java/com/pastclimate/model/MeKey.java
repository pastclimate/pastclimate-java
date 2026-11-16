package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The key the call was made with.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MeKey {
    @SerializedName("spend_order")
    private String spendOrder;

    /** Which credits this key spends first: "plan_first" or "topup_first". */
    public String getSpendOrder() {
        return spendOrder;
    }

    @Override
    public String toString() {
        return "MeKey{" + "spendOrder=" + spendOrder + "}";
    }
}
