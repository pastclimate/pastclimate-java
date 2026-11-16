package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 * Response metadata: credits used and left, plus the fields each endpoint adds. Fields an endpoint does not send are null.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class Meta {
    @SerializedName("credits")
    private Long credits;

    @SerializedName("credits_remaining")
    private Long creditsRemaining;

    @SerializedName("methodology_version")
    private String methodologyVersion;

    @SerializedName("query")
    private String query;

    @SerializedName("country")
    private String country;

    @SerializedName("count")
    private Integer count;

    @SerializedName("units")
    private MetaUnits units;

    @SerializedName("start")
    private LocalDate start;

    @SerializedName("end")
    private LocalDate end;

    @SerializedName("days")
    private Integer days;

    @SerializedName("date")
    private LocalDate date;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("rows")
    private Integer rows;

    @SerializedName("month")
    private Integer month;

    /** Credits this call used. 0 on an error. */
    public Long getCredits() {
        return credits;
    }

    /** Credits the next call can spend: the plan's remaining allowance plus bought credits. */
    public Long getCreditsRemaining() {
        return creditsRemaining;
    }

    /** Version of the data-processing methodology. Changes only when published numbers can shift. */
    public String getMethodologyVersion() {
        return methodologyVersion;
    }

    /** The search text. */
    public String getQuery() {
        return query;
    }

    /** The country filter, or null. */
    public String getCountry() {
        return country;
    }

    /** Results returned. */
    public Integer getCount() {
        return count;
    }

    public MetaUnits getUnits() {
        return units;
    }

    /** First day. */
    public LocalDate getStart() {
        return start;
    }

    /** Last day. */
    public LocalDate getEnd() {
        return end;
    }

    /** Rows returned. */
    public Integer getDays() {
        return days;
    }

    /** First day. */
    public LocalDate getDate() {
        return date;
    }

    /** Timezone of the local days. */
    public String getTimezone() {
        return timezone;
    }

    /** Rows returned. */
    public Integer getRows() {
        return rows;
    }

    /** The month filter, or null. */
    public Integer getMonth() {
        return month;
    }

    @Override
    public String toString() {
        return "Meta{" + "credits=" + credits + ", creditsRemaining=" + creditsRemaining + ", methodologyVersion=" + methodologyVersion + ", query=" + query + ", country=" + country + ", count=" + count + ", units=" + units + ", start=" + start + ", end=" + end + ", days=" + days + ", date=" + date + ", timezone=" + timezone + ", rows=" + rows + ", month=" + month + "}";
    }
}
