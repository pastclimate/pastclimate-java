package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The place a response is about (the "location" part of the envelope).
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class Location {
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

    /** Location id: country code, a slash, then the place slug. Use it as the location parameter. */
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

    /** Latitude of the centre of the grid cell that supplies the data (0.25 degree cells, about 28 km), not of the town centre. */
    public Double getLat() {
        return lat;
    }

    /** Longitude of the centre of the grid cell that supplies the data. */
    public Double getLon() {
        return lon;
    }

    /** IANA timezone. Daily and hourly days are local calendar days in this timezone. */
    public String getTimezone() {
        return timezone;
    }

    /** Present only when the location was given as lat,lon (or by /v1/locations/nearest): the point that was snapped. */
    public RequestedPoint getRequested() {
        return requested;
    }

    @Override
    public String toString() {
        return "Location{" + "id=" + id + ", name=" + name + ", country=" + country + ", lat=" + lat + ", lon=" + lon + ", timezone=" + timezone + ", requested=" + requested + "}";
    }
}
