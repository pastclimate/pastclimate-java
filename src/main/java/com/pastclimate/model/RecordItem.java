package com.pastclimate.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 * One extreme on record.
 *
 * <p>Every getter can return null: a field the server omits is null, and a field this
 * version does not know about is ignored.
 */
public final class RecordItem {
    @SerializedName("metric")
    private String metric;

    @SerializedName("value")
    private Double value;

    @SerializedName("unit")
    private String unit;

    @SerializedName("occurred_on")
    private LocalDate occurredOn;

    /** Which extreme. */
    public String getMetric() {
        return metric;
    }

    /** The extreme value, in the unit given by unit. */
    public Double getValue() {
        return value;
    }

    /** Unit of value. */
    public String getUnit() {
        return unit;
    }

    /** The day the extreme occurred. */
    public LocalDate getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String toString() {
        return "RecordItem{" + "metric=" + metric + ", value=" + value + ", unit=" + unit + ", occurredOn=" + occurredOn + "}";
    }
}
