package com.pastclimate;

import com.pastclimate.model.Location;
import com.pastclimate.model.Meta;
import java.util.Map;

/**
 * A JSON answer: {@code data} (typed models), {@code location}, {@code meta}, plus rate-limit and credit info.
 *
 * @param <T> the type of {@code data}
 */
public final class ApiResponse<T> extends BaseResponse {
    private final T data;
    private final Location location;

    ApiResponse(T data, Location location, Meta meta, int status, Map<String, String> headers) {
        super(status, headers, meta);
        this.data = data;
        this.location = location;
    }

    /** The rows or object you asked for. Lists are never null. */
    public T getData() {
        return data;
    }

    /** The place the answer is about, or null for calls that are not about one place. */
    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "ApiResponse{status=" + getStatus() + ", creditsUsed=" + getCreditsUsed()
                + ", creditsRemaining=" + getCreditsRemaining() + "}";
    }
}
