package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * One calendar month (UTC) of summary values.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class MonthlyRow {
    @SerializedName("year")
    private Integer year;

    @SerializedName("month")
    private Integer month;

    @SerializedName("monthly_avg_temp")
    private Double monthlyAvgTemp;

    @SerializedName("monthly_min_temp")
    private Double monthlyMinTemp;

    @SerializedName("monthly_max_temp")
    private Double monthlyMaxTemp;

    @SerializedName("monthly_total_rain")
    private Double monthlyTotalRain;

    @SerializedName("monthly_avg_wind")
    private Double monthlyAvgWind;

    @SerializedName("monthly_avg_pressure")
    private Double monthlyAvgPressure;

    @SerializedName("monthly_avg_cloud_percent")
    private Double monthlyAvgCloudPercent;

    @SerializedName("monthly_total_solar_j_m2")
    private Double monthlyTotalSolarJM2;

    @SerializedName("rainy_days")
    private Integer rainyDays;

    @SerializedName("heavy_rain_days")
    private Integer heavyRainDays;

    @SerializedName("extreme_heat_days")
    private Integer extremeHeatDays;

    @SerializedName("extreme_cold_days")
    private Integer extremeColdDays;

    @SerializedName("hours")
    private Integer hours;

    @SerializedName("days")
    private Integer days;

    /** Calendar year (UTC). */
    public Integer getYear() {
        return year;
    }

    /** Month, 1 to 12 (UTC). */
    public Integer getMonth() {
        return month;
    }

    /** Mean hourly temperature, deg C. With units=imperial: deg F. */
    public Double getMonthlyAvgTemp() {
        return monthlyAvgTemp;
    }

    /** Lowest hourly temperature in the month, deg C. With units=imperial: deg F. */
    public Double getMonthlyMinTemp() {
        return monthlyMinTemp;
    }

    /** Highest hourly temperature in the month, deg C. With units=imperial: deg F. */
    public Double getMonthlyMaxTemp() {
        return monthlyMaxTemp;
    }

    /** Total precipitation, mm. With units=imperial: inches. */
    public Double getMonthlyTotalRain() {
        return monthlyTotalRain;
    }

    /** Mean wind speed, m/s. With units=imperial: mph. */
    public Double getMonthlyAvgWind() {
        return monthlyAvgWind;
    }

    /** Mean surface pressure, hPa. With units=imperial: inHg. */
    public Double getMonthlyAvgPressure() {
        return monthlyAvgPressure;
    }

    /** Mean cloud cover, 0 to 100. */
    public Double getMonthlyAvgCloudPercent() {
        return monthlyAvgCloudPercent;
    }

    /** Total solar radiation, joules per square metre. Not changed by units. */
    public Double getMonthlyTotalSolarJM2() {
        return monthlyTotalSolarJM2;
    }

    /** Days with at least 1 mm of precipitation. */
    public Integer getRainyDays() {
        return rainyDays;
    }

    /** Days with at least 20 mm of precipitation. */
    public Integer getHeavyRainDays() {
        return heavyRainDays;
    }

    /** Days with a maximum of at least 40 deg C. A count, not changed by units. */
    public Integer getExtremeHeatDays() {
        return extremeHeatDays;
    }

    /** Days with a minimum of at most 0 deg C. A count, not changed by units. */
    public Integer getExtremeColdDays() {
        return extremeColdDays;
    }

    /** Hourly values that went into the month. */
    public Integer getHours() {
        return hours;
    }

    /** Days that went into the day counts. */
    public Integer getDays() {
        return days;
    }

    @Override
    public String toString() {
        return "MonthlyRow{" + "year=" + year + ", month=" + month + ", monthlyAvgTemp=" + monthlyAvgTemp + ", monthlyMinTemp=" + monthlyMinTemp + ", monthlyMaxTemp=" + monthlyMaxTemp + ", monthlyTotalRain=" + monthlyTotalRain + ", monthlyAvgWind=" + monthlyAvgWind + ", monthlyAvgPressure=" + monthlyAvgPressure + ", monthlyAvgCloudPercent=" + monthlyAvgCloudPercent + ", monthlyTotalSolarJM2=" + monthlyTotalSolarJM2 + ", rainyDays=" + rainyDays + ", heavyRainDays=" + heavyRainDays + ", extremeHeatDays=" + extremeHeatDays + ", extremeColdDays=" + extremeColdDays + ", hours=" + hours + ", days=" + days + "}";
    }
}
