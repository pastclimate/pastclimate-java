package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The point you asked for and how far it was from the grid cell that supplies the data.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class RequestedPoint {
    @SerializedName("lat")
    private Double lat;

    @SerializedName("lon")
    private Double lon;

    @SerializedName("distance_km")
    private Double distanceKm;

    /** Latitude you asked for. */
    public Double getLat() {
        return lat;
    }

    /** Longitude you asked for. */
    public Double getLon() {
        return lon;
    }

    /** Great-circle distance from the requested point to the snapped grid cell, in km. */
    public Double getDistanceKm() {
        return distanceKm;
    }

    @Override
    public String toString() {
        return "RequestedPoint{" + "lat=" + lat + ", lon=" + lon + ", distanceKm=" + distanceKm + "}";
    }
}
