package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The plan of the account the key belongs to.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MePlan {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("commercial_use")
    private Boolean commercialUse;

    @SerializedName("attribution_link_required")
    private Boolean attributionLinkRequired;

    @SerializedName("hourly_max_days_per_call")
    private Integer hourlyMaxDaysPerCall;

    @SerializedName("hourly_max_age_months")
    private Integer hourlyMaxAgeMonths;

    /** Plan id. */
    public String getId() {
        return id;
    }

    /** Plan name. */
    public String getName() {
        return name;
    }

    /** Whether commercial use is allowed. */
    public Boolean getCommercialUse() {
        return commercialUse;
    }

    /** Whether a visible link back is required. */
    public Boolean getAttributionLinkRequired() {
        return attributionLinkRequired;
    }

    /** Most days one /v1/hourly call may ask for. */
    public Integer getHourlyMaxDaysPerCall() {
        return hourlyMaxDaysPerCall;
    }

    /** How far back hourly data goes, in months. null for full history. */
    public Integer getHourlyMaxAgeMonths() {
        return hourlyMaxAgeMonths;
    }

    @Override
    public String toString() {
        return "MePlan{" + "id=" + id + ", name=" + name + ", commercialUse=" + commercialUse + ", attributionLinkRequired=" + attributionLinkRequired + ", hourlyMaxDaysPerCall=" + hourlyMaxDaysPerCall + ", hourlyMaxAgeMonths=" + hourlyMaxAgeMonths + "}";
    }
}
