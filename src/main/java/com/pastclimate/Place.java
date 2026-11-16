package com.pastclimate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Where to get weather for: a location id, or a latitude/longitude plus country.
 *
 * <pre>{@code
 * Place.id("gb/london");
 * Place.point(51.5, -0.12, "gb");
 * }</pre>
 *
 * <p>A point is snapped to the nearest place in that country; the server echoes the snapped point in
 * {@code response.getLocation().getRequested()}.
 */
public final class Place {
    private static final Pattern POINT_LIKE = Pattern.compile("^\\s*-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?\\s*$");

    private final String location;
    private final String country;

    private Place(String location, String country) {
        this.location = location;
        this.country = country;
    }

    /** A location id: country code, a slash, then the place slug, for example {@code gb/london}. */
    public static Place id(String id) {
        String text = id == null ? "" : id.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("A location id is required, for example \"gb/london\".");
        }
        if (POINT_LIKE.matcher(text).matches()) {
            throw new IllegalArgumentException("\"" + text + "\" looks like a point; use Place.point(lat, lon, country).");
        }
        return new Place(text, null);
    }

    /** A latitude and longitude, with the two-letter country code they are in. */
    public static Place point(double lat, double lon, String country) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
            throw new IllegalArgumentException("lat and lon must be finite numbers.");
        }
        String cc = country == null ? "" : country.trim();
        if (cc.isEmpty()) {
            throw new IllegalArgumentException("A country is required with a lat/lon point, for example \"gb\".");
        }
        return new Place(formatNumber(lat) + "," + formatNumber(lon), cc);
    }

    /** The value of the {@code location} parameter: an id, or {@code lat,lon}. */
    public String getLocation() {
        return location;
    }

    /** The country of a point, or null for an id. */
    public String getCountry() {
        return country;
    }

    /** Plain decimal text, never scientific notation. */
    static String formatNumber(double value) {
        BigDecimal number = new BigDecimal(Double.toString(value));
        if (number.signum() == 0) {
            return "0";
        }
        return number.stripTrailingZeros().toPlainString();
    }

    Map<String, String> toQuery() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("location", location);
        if (country != null) {
            params.put("country", country);
        }
        return params;
    }

    @Override
    public String toString() {
        return country == null ? location : location + " (" + country + ")";
    }
}
