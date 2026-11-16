package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;

/**
 * One hour of weather.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class HourlyRow {
    @SerializedName("timestamp_utc")
    private Instant timestampUtc;

    @SerializedName("temperature")
    private Double temperature;

    @SerializedName("feels_like_temperature")
    private Double feelsLikeTemperature;

    @SerializedName("dewpoint")
    private Double dewpoint;

    @SerializedName("precipitation")
    private Double precipitation;

    @SerializedName("wind_speed")
    private Double windSpeed;

    @SerializedName("wind_direction_deg")
    private Double windDirectionDeg;

    @SerializedName("relative_humidity_percent")
    private Double relativeHumidityPercent;

    @SerializedName("pressure")
    private Double pressure;

    @SerializedName("cloud_cover_percent")
    private Integer cloudCoverPercent;

    @SerializedName("solar_radiation_kwh_m2")
    private Double solarRadiationKwhM2;

    /** Start of the hour, UTC. */
    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    /** Air temperature at 2 m, deg C. With units=imperial: deg F. */
    public Double getTemperature() {
        return temperature;
    }

    /** Apparent temperature (Steadman), deg C. With units=imperial: deg F. */
    public Double getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    /** Dewpoint, deg C. With units=imperial: deg F. */
    public Double getDewpoint() {
        return dewpoint;
    }

    /** Precipitation in the hour, mm. With units=imperial: inches. */
    public Double getPrecipitation() {
        return precipitation;
    }

    /** Wind speed, m/s. With units=imperial: mph. */
    public Double getWindSpeed() {
        return windSpeed;
    }

    /** Wind direction, degrees, 0 to 360. */
    public Double getWindDirectionDeg() {
        return windDirectionDeg;
    }

    /** Relative humidity, 0 to 100, derived from temperature and dewpoint. */
    public Double getRelativeHumidityPercent() {
        return relativeHumidityPercent;
    }

    /** Surface pressure, hPa. With units=imperial: inHg. */
    public Double getPressure() {
        return pressure;
    }

    /** Total cloud cover, 0 to 100. */
    public Integer getCloudCoverPercent() {
        return cloudCoverPercent;
    }

    /** Solar radiation received in the hour, kWh per square metre. Not changed by units. */
    public Double getSolarRadiationKwhM2() {
        return solarRadiationKwhM2;
    }

    @Override
    public String toString() {
        return "HourlyRow{" + "timestampUtc=" + timestampUtc + ", temperature=" + temperature + ", feelsLikeTemperature=" + feelsLikeTemperature + ", dewpoint=" + dewpoint + ", precipitation=" + precipitation + ", windSpeed=" + windSpeed + ", windDirectionDeg=" + windDirectionDeg + ", relativeHumidityPercent=" + relativeHumidityPercent + ", pressure=" + pressure + ", cloudCoverPercent=" + cloudCoverPercent + ", solarRadiationKwhM2=" + solarRadiationKwhM2 + "}";
    }
}
