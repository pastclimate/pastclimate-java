package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * One location by id.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class LocationDetail {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("country")
    private String country;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lon")
    private Double lon;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("population")
    private Long population;

    @SerializedName("coverage")
    private LocationCoverage coverage;

    /** Location id. */
    public String getId() {
        return id;
    }

    /** Display name. */
    public String getName() {
        return name;
    }

    /** Lower-case two-letter country code. */
    public String getCountry() {
        return country;
    }

    /** Grid cell centre latitude. */
    public Double getLat() {
        return lat;
    }

    /** Grid cell centre longitude. */
    public Double getLon() {
        return lon;
    }

    /** IANA timezone. */
    public String getTimezone() {
        return timezone;
    }

    /** Population. */
    public Long getPopulation() {
        return population;
    }

    public LocationCoverage getCoverage() {
        return coverage;
    }

    @Override
    public String toString() {
        return "LocationDetail{" + "id=" + id + ", name=" + name + ", country=" + country + ", lat=" + lat + ", lon=" + lon + ", timezone=" + timezone + ", population=" + population + ", coverage=" + coverage + "}";
    }
}
