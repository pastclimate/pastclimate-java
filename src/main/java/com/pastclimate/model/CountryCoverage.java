package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 * Published coverage of one country.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class CountryCoverage {
    @SerializedName("country")
    private String country;

    @SerializedName("first_year")
    private Integer firstYear;

    @SerializedName("last_date")
    private LocalDate lastDate;

    /** Lower-case two-letter country code. */
    public String getCountry() {
        return country;
    }

    /** First year with published data. */
    public Integer getFirstYear() {
        return firstYear;
    }

    /** Last day with published data. */
    public LocalDate getLastDate() {
        return lastDate;
    }

    @Override
    public String toString() {
        return "CountryCoverage{" + "country=" + country + ", firstYear=" + firstYear + ", lastDate=" + lastDate + "}";
    }
}
