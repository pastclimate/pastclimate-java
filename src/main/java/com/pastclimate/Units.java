package com.pastclimate;

/** Unit system for values in a response. Metric is the server default. */
public enum Units {
    METRIC("metric"),
    IMPERIAL("imperial");

    private final String value;

    Units(String value) {
        this.value = value;
    }

    /** The text sent to the API. */
    public String value() {
        return value;
    }
}
