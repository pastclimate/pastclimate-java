package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Climate normal for one calendar day of the year.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class NormalsDayRow {
    @SerializedName("md")
    private String md;

    @SerializedName("avg_temp")
    private Double avgTemp;

    @SerializedName("min_temp")
    private Double minTemp;

    @SerializedName("max_temp")
    private Double maxTemp;

    @SerializedName("rain_prob")
    private Double rainProb;

    /** Calendar day as MM-DD. 02-29 is included. */
    public String getMd() {
        return md;
    }

    /** Mean daily mean temperature on this calendar day, deg C. With units=imperial: deg F. */
    public Double getAvgTemp() {
        return avgTemp;
    }

    /** Mean daily minimum, deg C. With units=imperial: deg F. */
    public Double getMinTemp() {
        return minTemp;
    }

    /** Mean daily maximum, deg C. With units=imperial: deg F. */
    public Double getMaxTemp() {
        return maxTemp;
    }

    /** Share of years, 0 to 1, with at least 1 mm of precipitation on this calendar day. */
    public Double getRainProb() {
        return rainProb;
    }

    @Override
    public String toString() {
        return "NormalsDayRow{" + "md=" + md + ", avgTemp=" + avgTemp + ", minTemp=" + minTemp + ", maxTemp=" + maxTemp + ", rainProb=" + rainProb + "}";
    }
}
