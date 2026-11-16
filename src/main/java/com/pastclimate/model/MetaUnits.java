package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * The unit system used for the values in a response.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MetaUnits {
    @SerializedName("system")
    private String system;

    @SerializedName("temperature")
    private String temperature;

    @SerializedName("precipitation")
    private String precipitation;

    @SerializedName("wind_speed")
    private String windSpeed;

    @SerializedName("pressure")
    private String pressure;

    /** The unit system used for the values in this response. */
    public String getSystem() {
        return system;
    }

    /** Temperature unit. */
    public String getTemperature() {
        return temperature;
    }

    /** Precipitation unit. */
    public String getPrecipitation() {
        return precipitation;
    }

    /** Wind speed unit. */
    public String getWindSpeed() {
        return windSpeed;
    }

    /** Pressure unit. */
    public String getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return "MetaUnits{" + "system=" + system + ", temperature=" + temperature + ", precipitation=" + precipitation + ", windSpeed=" + windSpeed + ", pressure=" + pressure + "}";
    }
}
