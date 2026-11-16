package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The place nearest a point.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class NearestLocation {
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

    @SerializedName("requested")
    private RequestedPoint requested;

    @SerializedName("population")
    private Long population;

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

    /** Present only when the location was given as lat,lon (or by /v1/locations/nearest): the point that was snapped. */
    public RequestedPoint getRequested() {
        return requested;
    }

    /** Population. */
    public Long getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        return "NearestLocation{" + "id=" + id + ", name=" + name + ", country=" + country + ", lat=" + lat + ", lon=" + lon + ", timezone=" + timezone + ", requested=" + requested + ", population=" + population + "}";
    }
}
