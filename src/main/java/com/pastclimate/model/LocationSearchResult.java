package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * One result of a place search.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class LocationSearchResult {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("country")
    private String country;

    @SerializedName("population")
    private Long population;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lon")
    private Double lon;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("weather_available")
    private Boolean weatherAvailable;

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

    /** Population, used to rank results. */
    public Long getPopulation() {
        return population;
    }

    /** Grid cell centre latitude. Absent when weather_available is false. */
    public Double getLat() {
        return lat;
    }

    /** Grid cell centre longitude. Absent when weather_available is false. */
    public Double getLon() {
        return lon;
    }

    /** IANA timezone. Absent when weather_available is false. */
    public String getTimezone() {
        return timezone;
    }

    /** false when this place cannot be served. Asking for its data returns 422 location_blocked. */
    public Boolean getWeatherAvailable() {
        return weatherAvailable;
    }

    @Override
    public String toString() {
        return "LocationSearchResult{" + "id=" + id + ", name=" + name + ", country=" + country + ", population=" + population + ", lat=" + lat + ", lon=" + lon + ", timezone=" + timezone + ", weatherAvailable=" + weatherAvailable + "}";
    }
}
