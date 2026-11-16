package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Climate normal for one month.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class NormalsMonthlyRow {
    @SerializedName("month")
    private Integer month;

    @SerializedName("avg_temp")
    private Double avgTemp;

    @SerializedName("min_temp")
    private Double minTemp;

    @SerializedName("max_temp")
    private Double maxTemp;

    @SerializedName("total_rain")
    private Double totalRain;

    @SerializedName("rainy_days")
    private Double rainyDays;

    @SerializedName("avg_wind")
    private Double avgWind;

    @SerializedName("avg_cloud_pct")
    private Double avgCloudPct;

    @SerializedName("avg_solar_kwh_m2")
    private Double avgSolarKwhM2;

    @SerializedName("years_covered")
    private Integer yearsCovered;

    @SerializedName("avg_humidity_pct")
    private Double avgHumidityPct;

    /** Month, 1 to 12. */
    public Integer getMonth() {
        return month;
    }

    /** Mean daily mean temperature, deg C. With units=imperial: deg F. */
    public Double getAvgTemp() {
        return avgTemp;
    }

    /** Mean daily minimum temperature, deg C. With units=imperial: deg F. */
    public Double getMinTemp() {
        return minTemp;
    }

    /** Mean daily maximum temperature, deg C. With units=imperial: deg F. */
    public Double getMaxTemp() {
        return maxTemp;
    }

    /** Mean monthly precipitation total, mm. With units=imperial: inches. */
    public Double getTotalRain() {
        return totalRain;
    }

    /** Mean number of days with at least 1 mm of precipitation. */
    public Double getRainyDays() {
        return rainyDays;
    }

    /** Mean wind speed, m/s. With units=imperial: mph. */
    public Double getAvgWind() {
        return avgWind;
    }

    /** Mean cloud cover, 0 to 100. */
    public Double getAvgCloudPct() {
        return avgCloudPct;
    }

    /** Mean daily solar radiation, kWh per square metre. Not changed by units. */
    public Double getAvgSolarKwhM2() {
        return avgSolarKwhM2;
    }

    /** Years of data the normal is averaged over. */
    public Integer getYearsCovered() {
        return yearsCovered;
    }

    /** Mean relative humidity, 0 to 100. */
    public Double getAvgHumidityPct() {
        return avgHumidityPct;
    }

    @Override
    public String toString() {
        return "NormalsMonthlyRow{" + "month=" + month + ", avgTemp=" + avgTemp + ", minTemp=" + minTemp + ", maxTemp=" + maxTemp + ", totalRain=" + totalRain + ", rainyDays=" + rainyDays + ", avgWind=" + avgWind + ", avgCloudPct=" + avgCloudPct + ", avgSolarKwhM2=" + avgSolarKwhM2 + ", yearsCovered=" + yearsCovered + ", avgHumidityPct=" + avgHumidityPct + "}";
    }
}
