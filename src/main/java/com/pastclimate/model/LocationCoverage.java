package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 * Span of published data for a location's country.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class LocationCoverage {
    @SerializedName("first_year")
    private Integer firstYear;

    @SerializedName("last_date")
    private LocalDate lastDate;

    /** First year with published data for the country. */
    public Integer getFirstYear() {
        return firstYear;
    }

    /** Last day with published data for the country. */
    public LocalDate getLastDate() {
        return lastDate;
    }

    @Override
    public String toString() {
        return "LocationCoverage{" + "firstYear=" + firstYear + ", lastDate=" + lastDate + "}";
    }
}
