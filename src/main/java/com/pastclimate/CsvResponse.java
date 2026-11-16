package com.pastclimate;

import java.util.Map;

/**
 * A CSV answer ({@code getDailyCsv}, {@code getHourlyCsv}). {@link #getCsv()} is the text. A CSV body has
 * no meta, so credit numbers come from the response headers.
 */
public final class CsvResponse extends BaseResponse {
    private final String csv;

    CsvResponse(String csv, int status, Map<String, String> headers) {
        super(status, headers, null);
        this.csv = csv;
    }

    /** The CSV text: one header row of field names, then one line per row. */
    public String getCsv() {
        return csv;
    }

    @Override
    public String toString() {
        return csv;
    }
}
