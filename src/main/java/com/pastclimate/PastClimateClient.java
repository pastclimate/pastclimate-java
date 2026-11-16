package com.pastclimate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pastclimate.model.CoverageData;
import com.pastclimate.model.DailyRow;
import com.pastclimate.model.HourlyRow;
import com.pastclimate.model.Location;
import com.pastclimate.model.LocationDetail;
import com.pastclimate.model.LocationSearchResult;
import com.pastclimate.model.MeData;
import com.pastclimate.model.Meta;
import com.pastclimate.model.MonthlyRow;
import com.pastclimate.model.NearestLocation;
import com.pastclimate.model.NormalsData;
import com.pastclimate.model.RecordItem;
import com.pastclimate.model.YearlyRow;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The PastClimate API client. One instance can be shared by many threads.
 *
 * <pre>{@code
 * PastClimateClient client = PastClimateClient.builder().build(); // reads PASTCLIMATE_API_KEY
 * ApiResponse<List<DailyRow>> response = client.getDaily(
 *         Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
 * response.getData().get(0).getDailyMaxTemp();
 * response.getCreditsRemaining();
 * }</pre>
 *
 * <p>Every JSON method returns an {@link ApiResponse} ({@code data}, {@code location}, {@code meta}, rate
 * limit and credit numbers). Daily and hourly data also have {@code ...Csv} methods that return a
 * {@link CsvResponse}.
 */
public final class PastClimateClient {
    public static final String DEFAULT_BASE_URL = "https://api.pastclimate.com";
    public static final String API_KEY_ENV = "PASTCLIMATE_API_KEY";
    public static final String BASE_URL_ENV = "PASTCLIMATE_BASE_URL";

    private static final long BACKOFF_BASE_MILLIS = 500;
    private static final List<String> LOOPBACK_HOSTS = Arrays.asList("localhost", "127.0.0.1", "[::1]", "::1");
    private static final Pattern DATE_ONLY = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private static final Type SEARCH_TYPE = new TypeToken<List<LocationSearchResult>>() { }.getType();
    private static final Type DAILY_TYPE = new TypeToken<List<DailyRow>>() { }.getType();
    private static final Type HOURLY_TYPE = new TypeToken<List<HourlyRow>>() { }.getType();
    private static final Type MONTHLY_TYPE = new TypeToken<List<MonthlyRow>>() { }.getType();
    private static final Type YEARLY_TYPE = new TypeToken<List<YearlyRow>>() { }.getType();
    private static final Type RECORDS_TYPE = new TypeToken<List<RecordItem>>() { }.getType();

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final Duration maxRetryDelay;
    private final HttpTransport transport;
    private final Sleeper sleeper;
    private final DoubleSupplier random;

    private PastClimateClient(Builder builder) {
        Function<String, String> env = builder.environment != null ? builder.environment : System::getenv;
        this.apiKey = cleanApiKey(builder.apiKey != null ? builder.apiKey : env.apply(API_KEY_ENV));
        String url = builder.baseUrl != null ? builder.baseUrl : env.apply(BASE_URL_ENV);
        this.baseUrl = cleanBaseUrl(url != null && !url.trim().isEmpty() ? url : DEFAULT_BASE_URL);
        if (builder.timeout.isZero() || builder.timeout.isNegative()) {
            throw new ConfigurationException("timeout must be a positive duration.");
        }
        if (builder.maxRetries < 0) {
            throw new ConfigurationException("maxRetries must be 0 or more.");
        }
        if (builder.maxRetryDelay.isNegative()) {
            throw new ConfigurationException("maxRetryDelay must be 0 or more.");
        }
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.maxRetryDelay = builder.maxRetryDelay;
        this.transport = builder.transport != null ? builder.transport : new JdkHttpTransport(builder.timeout);
        this.sleeper = builder.sleeper != null ? builder.sleeper : duration -> Thread.sleep(duration.toMillis());
        this.random = builder.random != null ? builder.random : () -> ThreadLocalRandom.current().nextDouble();
    }

    /** Starts building a client. */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds a {@link PastClimateClient}. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private Duration maxRetryDelay = Duration.ofSeconds(60);
        private HttpTransport transport;
        private Sleeper sleeper;
        private DoubleSupplier random;
        private Function<String, String> environment;

        private Builder() {
        }

        /** Your key. Default: the {@code PASTCLIMATE_API_KEY} environment variable. */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * The API base URL. Default: the {@code PASTCLIMATE_BASE_URL} environment variable, then
         * {@code https://api.pastclimate.com}.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /** How long to wait for each attempt. Default: 30 seconds. */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /** Retries after the first try. Default: 2. 0 turns retrying off. */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * The longest wait the client will sleep between tries. A 429 whose Retry-After is longer than this
         * is thrown, not waited out. Default: 60 seconds.
         */
        public Builder maxRetryDelay(Duration maxRetryDelay) {
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        /** A custom HTTP transport. Default: {@link JdkHttpTransport}. */
        public Builder transport(HttpTransport transport) {
            this.transport = transport;
            return this;
        }

        /** For tests: replaces the waiting between retries. */
        Builder sleeper(Sleeper sleeper) {
            this.sleeper = sleeper;
            return this;
        }

        /** For tests: replaces the random number (0 to 1) used for backoff jitter. */
        Builder random(DoubleSupplier random) {
            this.random = random;
            return this;
        }

        /** For tests: replaces System.getenv. */
        Builder environment(Function<String, String> environment) {
            this.environment = environment;
            return this;
        }

        public PastClimateClient build() {
            if (timeout == null || maxRetryDelay == null) {
                throw new ConfigurationException("timeout and maxRetryDelay must not be null.");
            }
            return new PastClimateClient(this);
        }
    }

    /** The base URL requests go to (no trailing slash). */
    public String getBaseUrl() {
        return baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getMaxRetryDelay() {
        return maxRetryDelay;
    }

    /** The key is never printed. */
    @Override
    public String toString() {
        return "PastClimateClient{baseUrl=" + baseUrl + ", apiKey=[REDACTED]}";
    }

    // --- Locations -----------------------------------------------------------

    /** Search places by the start of their name. */
    public ApiResponse<List<LocationSearchResult>> searchLocations(String q) {
        return searchLocations(q, null);
    }

    /**
     * Search places by the start of their name.
     *
     * @param q the start of a place name
     * @param country limit results to one country (two-letter code), or null
     */
    public ApiResponse<List<LocationSearchResult>> searchLocations(String q, String country) {
        if (q == null || q.trim().isEmpty()) {
            throw new IllegalArgumentException("q is required.");
        }
        Map<String, String> query = new LinkedHashMap<>();
        query.put("q", q);
        query.put("country", blankToNull(country));
        return getJson("/v1/locations/search", query, SEARCH_TYPE, true);
    }

    /** The place nearest a point, inside one country. */
    public ApiResponse<NearestLocation> nearestLocation(double lat, double lon, String country) {
        Place point = Place.point(lat, lon, country);
        Map<String, String> query = new LinkedHashMap<>();
        query.put("lat", Place.formatNumber(lat));
        query.put("lon", Place.formatNumber(lon));
        query.put("country", point.getCountry());
        return getJson("/v1/locations/nearest", query, NearestLocation.class, false);
    }

    /**
     * One location by id.
     *
     * @param id a location id such as {@code gb/london}
     */
    public ApiResponse<LocationDetail> getLocation(String id) {
        Place place = Place.id(id);
        String[] parts = place.getLocation().split("/", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("A location id looks like \"gb/london\".");
        }
        return getJson(
                "/v1/locations/" + encode(parts[0]) + "/" + encode(parts[1]),
                new LinkedHashMap<String, String>(),
                LocationDetail.class,
                false);
    }

    // --- Weather -------------------------------------------------------------

    /** Daily weather for each day from {@code start} to {@code end}, both included. */
    public ApiResponse<List<DailyRow>> getDaily(Place place, LocalDate start, LocalDate end) {
        return getDaily(place, start, end, null);
    }

    /**
     * Daily weather for each day from {@code start} to {@code end}, both included.
     *
     * @param units {@link Units#METRIC} (the server default) or {@link Units#IMPERIAL}; null for the default
     */
    public ApiResponse<List<DailyRow>> getDaily(Place place, LocalDate start, LocalDate end, Units units) {
        return getJson("/v1/daily", dailyQuery(place, start, end, units), DAILY_TYPE, true);
    }

    /** Daily weather as CSV. */
    public CsvResponse getDailyCsv(Place place, LocalDate start, LocalDate end) {
        return getDailyCsv(place, start, end, null);
    }

    /** Daily weather as CSV. */
    public CsvResponse getDailyCsv(Place place, LocalDate start, LocalDate end, Units units) {
        Map<String, String> query = dailyQuery(place, start, end, units);
        query.put("format", "csv");
        return getCsv("/v1/daily", query);
    }

    /** Hourly weather for one day, with the server's default number of days. */
    public ApiResponse<List<HourlyRow>> getHourly(Place place, LocalDate date) {
        return getJson("/v1/hourly", hourlyQuery(place, date, null, null), HOURLY_TYPE, true);
    }

    /** Hourly weather for {@code days} consecutive local days starting at {@code date}. */
    public ApiResponse<List<HourlyRow>> getHourly(Place place, LocalDate date, int days) {
        return getJson("/v1/hourly", hourlyQuery(place, date, days, null), HOURLY_TYPE, true);
    }

    /** Hourly weather for {@code days} consecutive local days starting at {@code date}. */
    public ApiResponse<List<HourlyRow>> getHourly(Place place, LocalDate date, int days, Units units) {
        return getJson("/v1/hourly", hourlyQuery(place, date, days, units), HOURLY_TYPE, true);
    }

    /** Hourly weather as CSV, with the server's default number of days. */
    public CsvResponse getHourlyCsv(Place place, LocalDate date) {
        Map<String, String> query = hourlyQuery(place, date, null, null);
        query.put("format", "csv");
        return getCsv("/v1/hourly", query);
    }

    /** Hourly weather as CSV. */
    public CsvResponse getHourlyCsv(Place place, LocalDate date, int days) {
        Map<String, String> query = hourlyQuery(place, date, days, null);
        query.put("format", "csv");
        return getCsv("/v1/hourly", query);
    }

    /** Hourly weather as CSV. */
    public CsvResponse getHourlyCsv(Place place, LocalDate date, int days, Units units) {
        Map<String, String> query = hourlyQuery(place, date, days, units);
        query.put("format", "csv");
        return getCsv("/v1/hourly", query);
    }

    /** One row per calendar month (UTC) for the whole archive. */
    public ApiResponse<List<MonthlyRow>> getMonthly(Place place) {
        return getMonthly(place, null);
    }

    /** One row per calendar month (UTC) for the whole archive. */
    public ApiResponse<List<MonthlyRow>> getMonthly(Place place, Units units) {
        return getJson("/v1/monthly", placeQuery(place, units), MONTHLY_TYPE, true);
    }

    /** One row per calendar year (UTC) for the whole archive. */
    public ApiResponse<List<YearlyRow>> getYearly(Place place) {
        return getYearly(place, null);
    }

    /** One row per calendar year (UTC) for the whole archive. */
    public ApiResponse<List<YearlyRow>> getYearly(Place place, Units units) {
        return getJson("/v1/yearly", placeQuery(place, units), YEARLY_TYPE, true);
    }

    /** Climate normals: 12 monthly and 366 day-of-year rows. */
    public ApiResponse<NormalsData> getNormals(Place place) {
        return getNormals(place, null, null);
    }

    /** Climate normals for one month (1 to 12). */
    public ApiResponse<NormalsData> getNormals(Place place, Integer month) {
        return getNormals(place, month, null);
    }

    /**
     * Climate normals.
     *
     * @param month 1 to 12 to narrow both lists to one month, or null for the whole year
     * @param units the unit system, or null for the server default
     */
    public ApiResponse<NormalsData> getNormals(Place place, Integer month, Units units) {
        Map<String, String> query = basePlaceQuery(place);
        query.put("month", monthParam(month));
        query.put("units", unitsParam(units));
        return getJson("/v1/normals", query, NormalsData.class, false);
    }

    /** The hottest, coldest, wettest and windiest day on file. */
    public ApiResponse<List<RecordItem>> getRecords(Place place) {
        return getRecords(place, null);
    }

    /** The hottest, coldest, wettest and windiest day on file. */
    public ApiResponse<List<RecordItem>> getRecords(Place place, Units units) {
        return getJson("/v1/records", placeQuery(place, units), RECORDS_TYPE, true);
    }

    // --- Account -------------------------------------------------------------

    /** The countries the archive serves, and the span of published data for each. */
    public ApiResponse<CoverageData> getCoverage() {
        return getCoverage(null);
    }

    /**
     * The countries the archive serves, and the span of published data for each.
     *
     * @param country only this country (two-letter code), or null for all
     */
    public ApiResponse<CoverageData> getCoverage(String country) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("country", blankToNull(country));
        return getJson("/v1/coverage", query, CoverageData.class, false);
    }

    /** Your plan, credit balance and limits. */
    public ApiResponse<MeData> getMe() {
        return getJson("/v1/me", new LinkedHashMap<String, String>(), MeData.class, false);
    }

    // --- Request plumbing ----------------------------------------------------

    private <T> ApiResponse<T> getJson(String path, Map<String, String> query, Type dataType, boolean list) {
        TransportResponse raw = perform(buildUri(path, query), requestHeaders(false));
        return parseEnvelope(raw, dataType, list);
    }

    private CsvResponse getCsv(String path, Map<String, String> query) {
        TransportResponse raw = perform(buildUri(path, query), requestHeaders(true));
        return new CsvResponse(raw.getBody(), raw.getStatus(), raw.getHeaders());
    }

    private Map<String, String> requestHeaders(boolean csv) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Accept", csv ? "text/csv, application/json;q=0.9" : "application/json");
        headers.put("User-Agent", "pastclimate-java/" + Version.VERSION);
        return headers;
    }

    private URI buildUri(String path, Map<String, String> query) {
        StringBuilder url = new StringBuilder(baseUrl).append(path);
        char separator = '?';
        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            url.append(separator).append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
            separator = '&';
        }
        return URI.create(url.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Sends the request, retrying where it is safe. Returns the 2xx answer or throws a typed exception. */
    private TransportResponse perform(URI uri, Map<String, String> headers) {
        int attempt = 0;
        while (true) {
            TransportResponse raw;
            try {
                raw = transport.get(uri, headers, timeout);
            } catch (ConnectionException e) {
                if (attempt >= maxRetries) {
                    throw e;
                }
                pause(backoffDelay(attempt));
                attempt++;
                continue;
            }

            raw = new TransportResponse(raw.getStatus(), Headers.normalize(raw.getHeaders()), raw.getBody());
            if (raw.getStatus() >= 200 && raw.getStatus() < 300) {
                return raw;
            }

            ApiException error = ErrorMapper.fromResponse(raw);
            Duration delay = retryDelay(error, attempt);
            if (delay == null) {
                throw error;
            }
            pause(delay);
            attempt++;
        }
    }

    /**
     * How long to wait before the next try, or null for "do not retry". A 429 is retried only when it
     * carries Retry-After (and the wait is not longer than maxRetryDelay). A 5xx is retried with backoff.
     * Other 4xx never.
     */
    private Duration retryDelay(ApiException error, int attempt) {
        if (attempt >= maxRetries) {
            return null;
        }
        if (error.getStatus() == 429) {
            Long wait = error.getRetryAfter();
            if (wait == null) {
                return null;
            }
            Duration requested = Duration.ofSeconds(wait);
            return requested.compareTo(maxRetryDelay) > 0 ? null : requested;
        }
        if (error.getStatus() >= 500) {
            return backoffDelay(attempt);
        }
        return null;
    }

    private Duration backoffDelay(int attempt) {
        long uncapped = attempt >= 20 ? Long.MAX_VALUE : BACKOFF_BASE_MILLIS * (1L << attempt);
        long capped = Math.min(uncapped, maxRetryDelay.toMillis());
        return Duration.ofMillis(Math.round(capped * (0.5 + 0.5 * random.getAsDouble())));
    }

    private void pause(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("Interrupted while waiting to retry.", false, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ApiResponse<T> parseEnvelope(TransportResponse raw, Type dataType, boolean list) {
        try {
            JsonElement root = JsonParser.parseString(raw.getBody());
            if (!root.isJsonObject()) {
                throw new IllegalStateException("expected a JSON object");
            }
            JsonObject object = root.getAsJsonObject();
            T data = Json.GSON.fromJson(object.get("data"), dataType);
            if (data == null && list) {
                data = (T) new ArrayList<Object>();
            }
            Location location = Json.GSON.fromJson(object.get("location"), Location.class);
            Meta meta = Json.GSON.fromJson(object.get("meta"), Meta.class);
            if (meta == null) {
                meta = new Meta();
            }
            return new ApiResponse<T>(data, location, meta, raw.getStatus(), raw.getHeaders());
        } catch (RuntimeException e) {
            throw new ServerException(
                    "The API answered " + raw.getStatus() + " but the body could not be read (" + e.getMessage() + ").",
                    raw.getStatus(),
                    "invalid_response",
                    null,
                    raw.getHeaders(),
                    raw.getBody());
        }
    }

    // --- Parameter handling --------------------------------------------------

    private static Map<String, String> basePlaceQuery(Place place) {
        if (place == null) {
            throw new IllegalArgumentException("A place is required: Place.id(\"gb/london\") or Place.point(...).");
        }
        return place.toQuery();
    }

    private static Map<String, String> placeQuery(Place place, Units units) {
        Map<String, String> query = basePlaceQuery(place);
        query.put("units", unitsParam(units));
        return query;
    }

    private static Map<String, String> dailyQuery(Place place, LocalDate start, LocalDate end, Units units) {
        Map<String, String> query = basePlaceQuery(place);
        query.put("start", dateParam(start, "start"));
        query.put("end", dateParam(end, "end"));
        query.put("units", unitsParam(units));
        return query;
    }

    private static Map<String, String> hourlyQuery(Place place, LocalDate date, Integer days, Units units) {
        Map<String, String> query = basePlaceQuery(place);
        query.put("date", dateParam(date, "date"));
        query.put("days", daysParam(days));
        query.put("units", unitsParam(units));
        return query;
    }

    private static String unitsParam(Units units) {
        return units == null ? null : units.value();
    }

    private static String dateParam(LocalDate date, String name) {
        if (date == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        String text = date.toString();
        if (!DATE_ONLY.matcher(text).matches()) {
            throw new IllegalArgumentException(name + " must be a year from 0000 to 9999, got " + text + ".");
        }
        return text;
    }

    private static String daysParam(Integer days) {
        if (days == null) {
            return null;
        }
        if (days < 1) {
            throw new IllegalArgumentException("days must be a whole number of at least 1.");
        }
        return days.toString();
    }

    private static String monthParam(Integer month) {
        if (month == null) {
            return null;
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be a whole number from 1 to 12.");
        }
        return month.toString();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    // --- Settings ------------------------------------------------------------

    private static String cleanApiKey(String value) {
        String key = value == null ? "" : value.trim();
        if (key.isEmpty()) {
            throw new ConfigurationException(
                    "No API key. Call apiKey(...) or set the " + API_KEY_ENV + " environment variable.");
        }
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c <= ' ' || c == 0x7f) {
                throw new ConfigurationException("The API key contains characters that are not allowed in a header.");
            }
        }
        return key;
    }

    private static String cleanBaseUrl(String value) {
        String text = value.trim().replaceAll("/+$", "");
        URI uri;
        try {
            uri = new URI(text);
        } catch (java.net.URISyntaxException e) {
            throw new ConfigurationException("baseUrl must be an http(s) URL, got \"" + value + "\".");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || !(scheme.equals("http") || scheme.equals("https")) || host == null || host.isEmpty()) {
            throw new ConfigurationException("baseUrl must be an http(s) URL, got \"" + value + "\".");
        }
        if (scheme.equals("http") && !LOOPBACK_HOSTS.contains(host)) {
            throw new ConfigurationException(
                    "baseUrl must use https (plain http is allowed only for localhost), got \"" + value + "\".");
        }
        return text;
    }
}
