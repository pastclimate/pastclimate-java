package com.pastclimate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Helpers for reading response headers (names are lower-cased). */
final class Headers {
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    private Headers() {
    }

    static Map<String, String> normalize(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    result.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** An integer header, or null when it is absent or not a whole number. */
    static Long integer(Map<String, String> headers, String name) {
        String value = headers.get(name);
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (!DIGITS.matcher(text).matches()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    /** Retry-After in whole seconds, or null. Only the delay-in-seconds form is read. */
    static Long retryAfter(Map<String, String> headers) {
        return integer(headers, "retry-after");
    }

    static Integer smallInteger(Map<String, String> headers, String name) {
        Long value = integer(headers, name);
        if (value == null || value > Integer.MAX_VALUE) {
            return null;
        }
        return value.intValue();
    }
}
