package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * One calendar year (UTC) of summary values.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class YearlyRow {
    @SerializedName("year")
    private Integer year;

    @SerializedName("yearly_avg_temp")
    private Double yearlyAvgTemp;

    @SerializedName("yearly_min_temp")
    private Double yearlyMinTemp;

    @SerializedName("yearly_max_temp")
    private Double yearlyMaxTemp;

    @SerializedName("yearly_total_rain")
    private Double yearlyTotalRain;

    @SerializedName("yearly_avg_wind")
    private Double yearlyAvgWind;

    @SerializedName("yearly_avg_pressure")
    private Double yearlyAvgPressure;

    @SerializedName("yearly_avg_cloud_percent")
    private Double yearlyAvgCloudPercent;

    @SerializedName("yearly_total_solar_j_m2")
    private Double yearlyTotalSolarJM2;

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

    @SerializedName("months")
    private Integer months;

    /** Calendar year (UTC). */
    public Integer getYear() {
        return year;
    }

    /** Mean hourly temperature, deg C. With units=imperial: deg F. */
    public Double getYearlyAvgTemp() {
        return yearlyAvgTemp;
    }

    /** Lowest hourly temperature in the year, deg C. With units=imperial: deg F. */
    public Double getYearlyMinTemp() {
        return yearlyMinTemp;
    }

    /** Highest hourly temperature in the year, deg C. With units=imperial: deg F. */
    public Double getYearlyMaxTemp() {
        return yearlyMaxTemp;
    }

    /** Total precipitation, mm. With units=imperial: inches. */
    public Double getYearlyTotalRain() {
        return yearlyTotalRain;
    }

    /** Mean wind speed, m/s. With units=imperial: mph. */
    public Double getYearlyAvgWind() {
        return yearlyAvgWind;
    }

    /** Mean surface pressure, hPa. With units=imperial: inHg. */
    public Double getYearlyAvgPressure() {
        return yearlyAvgPressure;
    }

    /** Mean cloud cover, 0 to 100. */
    public Double getYearlyAvgCloudPercent() {
        return yearlyAvgCloudPercent;
    }

    /** Total solar radiation, joules per square metre. Not changed by units. */
    public Double getYearlyTotalSolarJM2() {
        return yearlyTotalSolarJM2;
    }

    /** Days with at least 1 mm of precipitation. */
    public Integer getRainyDays() {
        return rainyDays;
    }

    /** Days with at least 20 mm of precipitation. */
    public Integer getHeavyRainDays() {
        return heavyRainDays;
    }

    /** Days with a maximum of at least 40 deg C. */
    public Integer getExtremeHeatDays() {
        return extremeHeatDays;
    }

    /** Days with a minimum of at most 0 deg C. */
    public Integer getExtremeColdDays() {
        return extremeColdDays;
    }

    /** Hourly values that went into the year. */
    public Integer getHours() {
        return hours;
    }

    /** Days that went into the day counts. */
    public Integer getDays() {
        return days;
    }

    /** Months that went into the year (12 for a complete year). */
    public Integer getMonths() {
        return months;
    }

    @Override
    public String toString() {
        return "YearlyRow{" + "year=" + year + ", yearlyAvgTemp=" + yearlyAvgTemp + ", yearlyMinTemp=" + yearlyMinTemp + ", yearlyMaxTemp=" + yearlyMaxTemp + ", yearlyTotalRain=" + yearlyTotalRain + ", yearlyAvgWind=" + yearlyAvgWind + ", yearlyAvgPressure=" + yearlyAvgPressure + ", yearlyAvgCloudPercent=" + yearlyAvgCloudPercent + ", yearlyTotalSolarJM2=" + yearlyTotalSolarJM2 + ", rainyDays=" + rainyDays + ", heavyRainDays=" + heavyRainDays + ", extremeHeatDays=" + extremeHeatDays + ", extremeColdDays=" + extremeColdDays + ", hours=" + hours + ", days=" + days + ", months=" + months + "}";
    }
}
