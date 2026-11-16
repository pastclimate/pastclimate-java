package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 * One local calendar day of weather.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class DailyRow {
    @SerializedName("date")
    private LocalDate date;

    @SerializedName("daily_avg_temp")
    private Double dailyAvgTemp;

    @SerializedName("daily_min_temp")
    private Double dailyMinTemp;

    @SerializedName("daily_max_temp")
    private Double dailyMaxTemp;

    @SerializedName("daily_total_rain")
    private Double dailyTotalRain;

    @SerializedName("daily_avg_wind")
    private Double dailyAvgWind;

    @SerializedName("daily_avg_cloud_percent")
    private Integer dailyAvgCloudPercent;

    @SerializedName("rain_flag")
    private Boolean rainFlag;

    /** The local calendar day, in the location's timezone. */
    public LocalDate getDate() {
        return date;
    }

    /** Mean temperature of the day, deg C. With units=imperial: deg F. */
    public Double getDailyAvgTemp() {
        return dailyAvgTemp;
    }

    /** Lowest temperature of the day, deg C. With units=imperial: deg F. */
    public Double getDailyMinTemp() {
        return dailyMinTemp;
    }

    /** Highest temperature of the day, deg C. With units=imperial: deg F. */
    public Double getDailyMaxTemp() {
        return dailyMaxTemp;
    }

    /** Total precipitation of the day, mm. With units=imperial: inches. */
    public Double getDailyTotalRain() {
        return dailyTotalRain;
    }

    /** Mean wind speed of the day, m/s. With units=imperial: mph. */
    public Double getDailyAvgWind() {
        return dailyAvgWind;
    }

    /** Mean cloud cover of the day, 0 to 100. */
    public Integer getDailyAvgCloudPercent() {
        return dailyAvgCloudPercent;
    }

    /** true when total precipitation was at least 1 mm. */
    public Boolean getRainFlag() {
        return rainFlag;
    }

    @Override
    public String toString() {
        return "DailyRow{" + "date=" + date + ", dailyAvgTemp=" + dailyAvgTemp + ", dailyMinTemp=" + dailyMinTemp + ", dailyMaxTemp=" + dailyMaxTemp + ", dailyTotalRain=" + dailyTotalRain + ", dailyAvgWind=" + dailyAvgWind + ", dailyAvgCloudPercent=" + dailyAvgCloudPercent + ", rainFlag=" + rainFlag + "}";
    }
}
