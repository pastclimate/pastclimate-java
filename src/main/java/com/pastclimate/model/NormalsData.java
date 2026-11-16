package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Monthly and day-of-year climate normals.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class NormalsData {
    @SerializedName("monthly")
    private List<NormalsMonthlyRow> monthly;

    @SerializedName("day_of_year")
    private List<NormalsDayRow> dayOfYear;

    /** 12 rows, or 1 when month is given. */
    public List<NormalsMonthlyRow> getMonthly() {
        return monthly;
    }

    /** 366 rows, or the days of that month when month is given. */
    public List<NormalsDayRow> getDayOfYear() {
        return dayOfYear;
    }

    @Override
    public String toString() {
        return "NormalsData{" + "monthly=" + monthly + ", dayOfYear=" + dayOfYear + "}";
    }
}
