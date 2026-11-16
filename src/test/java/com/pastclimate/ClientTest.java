package com.pastclimate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pastclimate.model.DailyRow;
import com.pastclimate.model.HourlyRow;
import com.pastclimate.model.LocationSearchResult;
import com.pastclimate.model.MeData;
import com.pastclimate.model.MonthlyRow;
import com.pastclimate.model.NormalsDayRow;
import com.pastclimate.model.NormalsMonthlyRow;
import com.pastclimate.model.RecordItem;
import com.pastclimate.model.YearlyRow;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClientTest extends TestSupport {
    private static final Place LONDON = Place.id("gb/london");
    private static final LocalDate JAN_1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate JAN_2 = LocalDate.of(2024, 1, 2);

    private static String path(FakeTransport.Request request) {
        return request.uri.getRawPath();
    }

    // --- one test per operation ------------------------------------------------

    @Test
    void searchLocations() {
        PastClimateClient client = client(okFixture("search.json"));
        ApiResponse<List<LocationSearchResult>> response = client.searchLocations("lon", "gb");

        assertEquals("/v1/locations/search", path(transport.last()));
        assertEquals(map("q", "lon", "country", "gb"), query(transport.last().uri));

        LocationSearchResult first = response.getData().get(0);
        LocationSearchResult second = response.getData().get(1);
        assertEquals("gb/london", first.getId());
        assertEquals(8982000L, first.getPopulation());
        assertEquals(Boolean.TRUE, first.getWeatherAvailable());
        assertEquals(Boolean.FALSE, second.getWeatherAvailable());
        assertNull(second.getPopulation());
        assertNull(second.getLat());
        assertNull(response.getLocation());
        assertEquals(2, response.getMeta().getCount());
        assertEquals("lon", response.getMeta().getQuery());
        assertNull(response.getMeta().getCountry());
    }

    @Test
    void searchWithoutCountryLeavesItOut() {
        client(okFixture("search.json")).searchLocations("lon");
        assertEquals(map("q", "lon"), query(transport.last().uri));
    }

    @Test
    void nearestLocation() {
        PastClimateClient client = client(okFixture("nearest.json"));
        ApiResponse<com.pastclimate.model.NearestLocation> response = client.nearestLocation(51.51, -0.12, "gb");

        assertEquals("/v1/locations/nearest", path(transport.last()));
        assertEquals(map("lat", "51.51", "lon", "-0.12", "country", "gb"), query(transport.last().uri));

        assertEquals("gb/london", response.getData().getId());
        assertEquals(3.2, response.getData().getRequested().getDistanceKm(), 0.0001);
        assertEquals(8982000L, response.getData().getPopulation());
        assertEquals(-0.12, response.getLocation().getRequested().getLon(), 0.0001);
    }

    @Test
    void getLocation() {
        PastClimateClient client = client(okFixture("location.json"));
        ApiResponse<com.pastclimate.model.LocationDetail> response = client.getLocation("gb/london");

        assertEquals("/v1/locations/gb/london", path(transport.last()));
        assertNull(transport.last().uri.getRawQuery());

        assertEquals(1990, response.getData().getCoverage().getFirstYear());
        assertEquals(LocalDate.of(2026, 10, 31), response.getData().getCoverage().getLastDate());
        assertEquals("Europe/London", response.getLocation().getTimezone());
    }

    @Test
    void getLocationEscapesThePath() {
        client(okFixture("location.json")).getLocation("gb/new york");
        assertEquals("/v1/locations/gb/new%20york", path(transport.last()));
    }

    @Test
    void getLocationNeedsAnId() {
        PastClimateClient client = client();
        assertThrows(IllegalArgumentException.class, () -> client.getLocation("london"));
        assertThrows(IllegalArgumentException.class, () -> client.getLocation(""));
        assertThrows(IllegalArgumentException.class, () -> client.getLocation(null));
        assertTrue(transport.requests.isEmpty());
    }

    @Test
    void getDailyWithImperialUnits() {
        PastClimateClient client = client(okFixture("daily.json"));
        ApiResponse<List<DailyRow>> response = client.getDaily(LONDON, JAN_1, JAN_2, Units.IMPERIAL);

        assertEquals("/v1/daily", path(transport.last()));
        assertEquals(
                map("location", "gb/london", "start", "2024-01-01", "end", "2024-01-02", "units", "imperial"),
                query(transport.last().uri));

        DailyRow first = response.getData().get(0);
        DailyRow second = response.getData().get(1);
        assertEquals(LocalDate.of(2024, 1, 1), first.getDate());
        assertEquals(5.2, first.getDailyAvgTemp(), 0.0001);
        assertEquals(78, first.getDailyAvgCloudPercent());
        assertEquals(Boolean.TRUE, first.getRainFlag());
        assertEquals(Boolean.FALSE, second.getRainFlag());
        assertEquals("metric", response.getMeta().getUnits().getSystem());
        assertEquals("C", response.getMeta().getUnits().getTemperature());
        assertEquals(LocalDate.of(2024, 1, 1), response.getMeta().getStart());
        assertEquals(LocalDate.of(2024, 1, 2), response.getMeta().getEnd());
        assertEquals(2, response.getMeta().getDays());
    }

    @Test
    void dailyWithoutUnitsLeavesThemOut() {
        client(okFixture("daily.json")).getDaily(LONDON, JAN_1, LocalDate.of(2024, 1, 31));
        assertEquals(
                map("location", "gb/london", "start", "2024-01-01", "end", "2024-01-31"),
                query(transport.last().uri));
    }

    @Test
    void dailyByPoint() {
        client(okFixture("daily.json")).getDaily(Place.point(51.5, -0.12, "gb"), JAN_1, JAN_2);
        Map<String, String> query = query(transport.last().uri);
        assertEquals("51.5,-0.12", query.get("location"));
        assertEquals("gb", query.get("country"));
    }

    @Test
    void pointNumbersArePlainDecimals() {
        assertEquals("0.00001,-120.5", Place.point(0.00001, -120.5, "gb").getLocation());
        assertEquals("0,10", Place.point(0.0, 10.0, "gb").getLocation());
        assertEquals("gb", Place.point(1, 2, " gb ").getCountry());
    }

    @Test
    void getHourly() {
        PastClimateClient client = client(okFixture("hourly.json"));
        ApiResponse<List<HourlyRow>> response = client.getHourly(LONDON, LocalDate.of(2024, 1, 15), 1, Units.METRIC);

        assertEquals("/v1/hourly", path(transport.last()));
        assertEquals(
                map("location", "gb/london", "date", "2024-01-15", "days", "1", "units", "metric"),
                query(transport.last().uri));

        HourlyRow row = response.getData().get(0);
        assertEquals(Instant.parse("2024-01-15T00:00:00Z"), row.getTimestampUtc());
        assertEquals(1012.4, row.getPressure(), 0.0001);
        assertEquals(90, row.getCloudCoverPercent());
        assertEquals("Europe/London", response.getMeta().getTimezone());
        assertEquals(LocalDate.of(2024, 1, 15), response.getMeta().getDate());
    }

    @Test
    void hourlyWithoutDaysLeavesItOut() {
        client(okFixture("hourly.json")).getHourly(LONDON, LocalDate.of(2024, 1, 15));
        assertEquals(map("location", "gb/london", "date", "2024-01-15"), query(transport.last().uri));
    }

    @Test
    void getMonthly() {
        PastClimateClient client = client(okFixture("monthly.json"));
        ApiResponse<List<MonthlyRow>> response = client.getMonthly(LONDON, Units.IMPERIAL);

        assertEquals("/v1/monthly", path(transport.last()));
        assertEquals(map("location", "gb/london", "units", "imperial"), query(transport.last().uri));

        MonthlyRow row = response.getData().get(0);
        assertEquals(2024, row.getYear());
        assertEquals(1, row.getMonth());
        assertEquals(15, row.getRainyDays());
        assertEquals(744, row.getHours());
        assertEquals(95000000.0, row.getMonthlyTotalSolarJM2(), 0.1);
        assertEquals(1, response.getMeta().getRows());
    }

    @Test
    void getYearly() {
        PastClimateClient client = client(okFixture("yearly.json"));
        ApiResponse<List<YearlyRow>> response = client.getYearly(LONDON);

        assertEquals("/v1/yearly", path(transport.last()));
        assertEquals(map("location", "gb/london"), query(transport.last().uri));

        YearlyRow row = response.getData().get(0);
        assertEquals(2023, row.getYear());
        assertEquals(12, row.getMonths());
        assertEquals(365, row.getDays());
    }

    @Test
    void getNormalsWithAMonth() {
        PastClimateClient client = client(okFixture("normals.json"));
        ApiResponse<com.pastclimate.model.NormalsData> response = client.getNormals(LONDON, 7);

        assertEquals("/v1/normals", path(transport.last()));
        assertEquals(map("location", "gb/london", "month", "7"), query(transport.last().uri));

        NormalsMonthlyRow month = response.getData().getMonthly().get(0);
        assertEquals(7, month.getMonth());
        assertEquals(30, month.getYearsCovered());
        assertNull(month.getAvgHumidityPct());
        NormalsDayRow day = response.getData().getDayOfYear().get(1);
        assertEquals("07-02", day.getMd());
        assertEquals(0.25, day.getRainProb(), 0.0001);
        assertEquals(7, response.getMeta().getMonth());
    }

    @Test
    void getNormalsWithoutAMonthLeavesItOut() {
        client(okFixture("normals.json")).getNormals(LONDON);
        assertEquals(map("location", "gb/london"), query(transport.last().uri));
    }

    @Test
    void getRecords() {
        PastClimateClient client = client(okFixture("records.json"));
        ApiResponse<List<RecordItem>> response = client.getRecords(LONDON);

        assertEquals("/v1/records", path(transport.last()));

        List<String> metrics = new ArrayList<>();
        for (RecordItem item : response.getData()) {
            metrics.add(item.getMetric());
        }
        assertEquals(List.of("hottest_day", "coldest_day", "wettest_day", "windiest_day"), metrics);
        RecordItem hottest = response.getData().get(0);
        assertEquals(37.9, hottest.getValue(), 0.0001);
        assertEquals("C", hottest.getUnit());
        assertEquals(LocalDate.of(2022, 7, 19), hottest.getOccurredOn());
        assertNull(response.getData().get(2).getOccurredOn());
    }

    @Test
    void getCoverage() {
        PastClimateClient client = client(okFixture("coverage.json"));
        ApiResponse<com.pastclimate.model.CoverageData> response = client.getCoverage("gb");

        assertEquals("/v1/coverage", path(transport.last()));
        assertEquals(map("country", "gb"), query(transport.last().uri));

        assertEquals(2, response.getData().getCountries().size());
        assertEquals("gb", response.getData().getCountries().get(0).getCountry());
        assertEquals(LocalDate.of(2026, 10, 31), response.getData().getCountries().get(0).getLastDate());
        assertNull(response.getLocation());
    }

    @Test
    void getCoverageWithoutACountry() {
        client(okFixture("coverage.json")).getCoverage();
        assertNull(transport.last().uri.getRawQuery());
    }

    @Test
    void getMe() {
        PastClimateClient client = client(okFixture("me.json"));
        ApiResponse<MeData> response = client.getMe();

        assertEquals("/v1/me", path(transport.last()));
        assertNull(transport.last().uri.getRawQuery());

        MeData me = response.getData();
        assertEquals("free", me.getPlan().getId());
        assertEquals(Boolean.FALSE, me.getPlan().getCommercialUse());
        assertEquals(Boolean.TRUE, me.getPlan().getAttributionLinkRequired());
        assertEquals(3, me.getPlan().getHourlyMaxDaysPerCall());
        assertEquals(5, me.getPlan().getHourlyMaxAgeMonths());
        assertEquals("plan_first", me.getKey().getSpendOrder());
        assertEquals(321L, me.getCredits().getOneTimeLimit());
        assertEquals(300L, me.getCredits().getOneTimeRemaining());
        assertNull(me.getCredits().getMonthlyLimit());
        assertNull(me.getCredits().getMonthResetsAt());
        assertEquals(Instant.parse("2026-11-15T00:00:00Z"), me.getCredits().getDayResetsAt());
        assertEquals(11L, me.getCredits().getTopupBalance());
        assertEquals(4L, me.getCredits().getTopupUsed());
        assertEquals(17, me.getRateLimit().getPerMinute());
        assertEquals(16, me.getRateLimit().getRemaining());
    }

    // --- meta, credits and rate limits on every response ----------------------

    private static final String[] ORDER = {
        "search", "nearest", "location", "daily", "hourly", "monthly", "yearly", "normals", "records", "coverage", "me"
    };

    private Object[] allFixtureAnswers() {
        Object[] answers = new Object[ORDER.length];
        for (int i = 0; i < ORDER.length; i++) {
            answers[i] = okFixture(ORDER[i] + ".json");
        }
        return answers;
    }

    private List<BaseResponse> callEverything(PastClimateClient client) {
        List<BaseResponse> responses = new ArrayList<>();
        responses.add(client.searchLocations("lon", "gb"));
        responses.add(client.nearestLocation(51.5, -0.12, "gb"));
        responses.add(client.getLocation("gb/london"));
        responses.add(client.getDaily(LONDON, JAN_1, JAN_2, Units.IMPERIAL));
        responses.add(client.getHourly(LONDON, LocalDate.of(2024, 1, 15), 1));
        responses.add(client.getMonthly(LONDON));
        responses.add(client.getYearly(LONDON));
        responses.add(client.getNormals(LONDON, 7));
        responses.add(client.getRecords(LONDON));
        responses.add(client.getCoverage("gb"));
        responses.add(client.getMe());
        return responses;
    }

    @Test
    void metaCreditsAndRateLimitAreOnEveryResponse() {
        List<BaseResponse> responses = callEverything(client(allFixtureAnswers()));

        assertEquals(11, responses.size());
        for (BaseResponse r : responses) {
            assertEquals("test-1", r.getMeta().getMethodologyVersion());
            assertEquals(1234L, r.getMeta().getCreditsRemaining());
            assertEquals(1234L, r.getCreditsRemaining());
            assertNotNull(r.getCreditsUsed());
            assertEquals(new RateLimit(17, 16, 42), r.getRateLimit());
            assertEquals(200, r.getStatus());
            assertEquals("17", r.getHeaders().get("ratelimit-limit"));
        }
        assertEquals(7L, responses.get(3).getCreditsUsed());
        assertEquals(0L, responses.get(0).getCreditsUsed());
    }

    @Test
    void creditsFallBackToHeadersWhenMetaHasNone() {
        String body = "{\"location\":null,\"data\":[],\"meta\":{\"methodology_version\":\"x\"}}";
        ApiResponse<List<LocationSearchResult>> r = client(ok(body, "x-credits-used", "9", "x-credits-remaining", "55"))
                .searchLocations("lon");
        assertEquals(9L, r.getCreditsUsed());
        assertEquals(55L, r.getCreditsRemaining());
    }

    @Test
    void rateLimitValuesAreNullWhenHeadersAreMissing() {
        TransportResponse answer = new TransportResponse(200, new HashMap<String, String>(), fixture("coverage.json"));
        ApiResponse<com.pastclimate.model.CoverageData> r = client(answer).getCoverage();
        assertEquals(new RateLimit(null, null, null), r.getRateLimit());
        assertNull(r.getRateLimit().getLimit());
    }

    @Test
    void headerNamesAreCaseInsensitive() {
        String body = "{\"location\":null,\"data\":[],\"meta\":{}}";
        TransportResponse answer =
                new TransportResponse(200, map("RateLimit-Limit", "9", "X-Credits-Used", "3"), body);
        ApiResponse<List<LocationSearchResult>> r = client(answer).searchLocations("lon");
        assertEquals(9, r.getRateLimit().getLimit());
        assertEquals(3L, r.getCreditsUsed());
    }

    @Test
    void unknownFieldsAreIgnored() {
        String body = "{\"location\":null,\"meta\":{\"credits\":0,\"brand_new\":1},"
                + "\"data\":[{\"id\":\"gb/london\",\"name\":\"London\",\"country\":\"gb\",\"population\":1,"
                + "\"weather_available\":true,\"shiny_new_field\":\"x\"}]}";
        ApiResponse<List<LocationSearchResult>> r = client(ok(body)).searchLocations("lon");
        assertEquals("gb/london", r.getData().get(0).getId());
    }

    @Test
    void aMissingDataListIsEmptyNotNull() {
        String body = "{\"location\":null,\"meta\":{}}";
        ApiResponse<List<LocationSearchResult>> r = client(ok(body)).searchLocations("lon");
        assertTrue(r.getData().isEmpty());
    }

    // --- auth, user agent, no key in the URL -----------------------------------

    @Test
    void sendsTheKeyInTheAuthorizationHeaderAndAUserAgent() {
        client(okFixture("me.json")).getMe();
        Map<String, String> headers = transport.last().headers;
        assertEquals("Bearer " + TEST_KEY, headers.get("Authorization"));
        assertEquals("pastclimate-java/" + Version.VERSION, headers.get("User-Agent"));
        assertEquals("application/json", headers.get("Accept"));
    }

    @Test
    void theKeyIsNeverInTheUrl() {
        PastClimateClient client = client(allFixtureAnswers());
        callEverything(client);

        assertEquals(11, transport.requests.size());
        for (FakeTransport.Request request : transport.requests) {
            assertFalse(request.uri.toString().contains(TEST_KEY));
            String q = request.uri.getRawQuery() == null ? "" : request.uri.getRawQuery().toLowerCase();
            assertFalse(q.contains("key") || q.contains("token") || q.contains("authorization"), q);
            assertEquals("Bearer " + TEST_KEY, request.headers.get("Authorization"));
        }
    }

    @Test
    void theClientNeverPrintsTheKey() {
        PastClimateClient client = client();
        assertFalse(client.toString().contains(TEST_KEY));
    }

    // --- configuration ---------------------------------------------------------

    @Test
    void keyComesFromTheEnvironment() {
        Map<String, String> env = map(PastClimateClient.API_KEY_ENV, "env-key-123");
        FakeTransport fake = new FakeTransport(okFixture("me.json"));
        PastClimateClient.builder().transport(fake).environment(env::get).build().getMe();
        assertEquals("Bearer env-key-123", fake.last().headers.get("Authorization"));
    }

    @Test
    void anExplicitKeyBeatsTheEnvironment() {
        Map<String, String> env = map(PastClimateClient.API_KEY_ENV, "env-key-123");
        FakeTransport fake = new FakeTransport(okFixture("me.json"));
        PastClimateClient.builder().apiKey("arg-key-456").transport(fake).environment(env::get).build().getMe();
        assertEquals("Bearer arg-key-456", fake.last().headers.get("Authorization"));
    }

    @Test
    void noKeyThrowsAConfigurationException() {
        ConfigurationException e = assertThrows(
                ConfigurationException.class,
                () -> PastClimateClient.builder().environment(name -> null).transport(new FakeTransport()).build());
        assertTrue(e.getMessage().contains("PASTCLIMATE_API_KEY"));
        assertThrows(
                ConfigurationException.class,
                () -> PastClimateClient.builder().apiKey("  ").environment(name -> null).build());
    }

    @Test
    void aKeyWithANewlineIsRefused() {
        assertThrows(
                ConfigurationException.class,
                () -> PastClimateClient.builder().apiKey("abc\r\nX-Evil: 1").transport(new FakeTransport()).build());
    }

    @Test
    void defaultAndCustomBaseUrls() {
        PastClimateClient plain = PastClimateClient.builder()
                .apiKey("k").transport(new FakeTransport()).environment(name -> null).build();
        assertEquals("https://api.pastclimate.com", plain.getBaseUrl());

        PastClimateClient custom = builder(okFixture("me.json")).baseUrl("https://example.test/").build();
        custom.getMe();
        assertEquals("https://example.test", custom.getBaseUrl());
        assertEquals("https://example.test/v1/me", transport.last().uri.toString());
    }

    @Test
    void baseUrlFromTheEnvironment() {
        Map<String, String> env = map(PastClimateClient.BASE_URL_ENV, "https://staging.example.test");
        PastClimateClient client = PastClimateClient.builder()
                .apiKey("k").transport(new FakeTransport()).environment(env::get).build();
        assertEquals("https://staging.example.test", client.getBaseUrl());
    }

    @Test
    void plainHttpIsAllowedOnlyForLocalhost() {
        assertEquals("http://127.0.0.1:8787", builder().baseUrl("http://127.0.0.1:8787").build().getBaseUrl());
        assertEquals("http://localhost:8787", builder().baseUrl("http://localhost:8787").build().getBaseUrl());
        for (String bad : new String[] {"http://api.example.test", "ftp://example.test", "not a url"}) {
            assertThrows(ConfigurationException.class, () -> builder().baseUrl(bad).build(), bad);
        }
    }

    @Test
    void defaultsAndSettings() {
        PastClimateClient client = client();
        assertEquals(Duration.ofSeconds(30), client.getTimeout());
        assertEquals(2, client.getMaxRetries());
        assertEquals(Duration.ofSeconds(60), client.getMaxRetryDelay());

        PastClimateClient custom = builder()
                .timeout(Duration.ofSeconds(5)).maxRetries(4).maxRetryDelay(Duration.ofSeconds(10)).build();
        assertEquals(Duration.ofSeconds(5), custom.getTimeout());
        assertEquals(4, custom.getMaxRetries());
        assertEquals(Duration.ofSeconds(10), custom.getMaxRetryDelay());

        assertThrows(ConfigurationException.class, () -> builder().timeout(Duration.ZERO).build());
        assertThrows(ConfigurationException.class, () -> builder().maxRetries(-1).build());
        assertThrows(ConfigurationException.class, () -> builder().maxRetryDelay(Duration.ofSeconds(-1)).build());
    }

    @Test
    void theTimeoutIsPassedToTheTransport() {
        builder(okFixture("me.json")).timeout(Duration.ofSeconds(12)).build().getMe();
        assertEquals(Duration.ofSeconds(12), transport.last().timeout);
    }

    // --- parameter checks (nothing is sent for a bad call) ---------------------

    @Test
    void badParametersThrowBeforeAnyRequest() {
        PastClimateClient client = client();
        assertThrows(IllegalArgumentException.class, () -> client.searchLocations(""));
        assertThrows(IllegalArgumentException.class, () -> client.searchLocations(null));
        assertThrows(IllegalArgumentException.class, () -> client.getNormals(LONDON, 13));
        assertThrows(IllegalArgumentException.class, () -> client.getNormals(LONDON, 0));
        assertThrows(IllegalArgumentException.class, () -> client.getHourly(LONDON, JAN_1, 0));
        assertThrows(IllegalArgumentException.class, () -> client.getMonthly(null));
        assertThrows(IllegalArgumentException.class, () -> client.getDaily(LONDON, null, JAN_2));
        assertThrows(IllegalArgumentException.class, () -> client.getDaily(LONDON, JAN_1, null));
        assertThrows(IllegalArgumentException.class, () -> client.nearestLocation(51.5, -0.12, ""));
        assertThrows(IllegalArgumentException.class, () -> client.nearestLocation(Double.NaN, -0.12, "gb"));
        assertThrows(IllegalArgumentException.class, () -> Place.id(""));
        assertThrows(IllegalArgumentException.class, () -> Place.id("51.5,-0.12"));
        assertThrows(IllegalArgumentException.class, () -> Place.point(1, 2, ""));
        assertTrue(transport.requests.isEmpty());
    }

    // --- CSV ---------------------------------------------------------------------

    @Test
    void dailyCsv() {
        CsvResponse response = client(ok(fixture("daily.csv"), "content-type", "text/csv"))
                .getDailyCsv(LONDON, JAN_1, JAN_2);

        assertEquals("/v1/daily", path(transport.last()));
        assertEquals("csv", query(transport.last().uri).get("format"));
        assertTrue(transport.last().headers.get("Accept").contains("text/csv"));

        assertEquals(fixture("daily.csv"), response.getCsv());
        assertEquals(fixture("daily.csv"), response.toString());
        assertEquals(7L, response.getCreditsUsed());
        assertEquals(1234L, response.getCreditsRemaining());
        assertEquals(16, response.getRateLimit().getRemaining());
        assertEquals(200, response.getStatus());
        assertNull(response.getMeta());
    }

    @Test
    void hourlyCsvWithUnits() {
        CsvResponse response = client(ok(fixture("hourly.csv"), "content-type", "text/csv"))
                .getHourlyCsv(LONDON, LocalDate.of(2024, 1, 15), 1, Units.IMPERIAL);
        assertEquals(
                map("location", "gb/london", "date", "2024-01-15", "days", "1", "units", "imperial", "format", "csv"),
                query(transport.last().uri));
        assertEquals(fixture("hourly.csv"), response.getCsv());
    }

    @Test
    void csvWithTheShorterOverloads() {
        PastClimateClient client = client(ok(fixture("daily.csv")), ok(fixture("hourly.csv")), ok(fixture("hourly.csv")));
        assertNotNull(client.getDailyCsv(LONDON, JAN_1, JAN_2, Units.METRIC));
        assertNotNull(client.getHourlyCsv(LONDON, JAN_1));
        assertNotNull(client.getHourlyCsv(LONDON, JAN_1, 2));
    }

    @Test
    void jsonRequestsDoNotSendAFormat() {
        client(okFixture("daily.json")).getDaily(LONDON, JAN_1, JAN_2);
        assertFalse(query(transport.last().uri).containsKey("format"));
    }

    @Test
    void anErrorOnACsvRequestIsStillATypedException() {
        PastClimateClient client = client(errorAnswerWithMessage(404, "not_published", "No published data for this request."));
        NotFoundException e = assertThrows(
                NotFoundException.class, () -> client.getDailyCsv(LONDON, LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 2)));
        assertEquals("not_published", e.getCode());
    }

    // --- success body problems ------------------------------------------------

    @Test
    void anUnreadableSuccessBodyIsAServerException() {
        ServerException e = assertThrows(ServerException.class, () -> client(ok("<html>hello</html>")).getMe());
        assertEquals("invalid_response", e.getCode());
        assertEquals(200, e.getStatus());

        assertThrows(ServerException.class, () -> client(ok("[1,2]")).getMe());
        assertThrows(ServerException.class, () -> client(ok("")).getMe());
        assertInstanceOf(ApiException.class, e);
    }

    @Test
    void aBadDateInASuccessBodyIsAServerException() {
        String body = "{\"location\":null,\"meta\":{},\"data\":[{\"date\":\"not-a-date\"}]}";
        assertThrows(ServerException.class, () -> client(ok(body)).getDaily(LONDON, JAN_1, JAN_2));
    }
}
