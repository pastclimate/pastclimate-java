package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * The countries the archive serves.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class CoverageData {
    @SerializedName("countries")
    private List<CountryCoverage> countries;

    public List<CountryCoverage> getCountries() {
        return countries;
    }

    @Override
    public String toString() {
        return "CoverageData{" + "countries=" + countries + "}";
    }
}
