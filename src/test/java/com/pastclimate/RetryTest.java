package com.pastclimate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pastclimate.model.MeData;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class RetryTest extends TestSupport {
    private static TransportResponse me() {
        return okFixture("me.json");
    }

    private static TransportResponse limited(String after) {
        return errorAnswerWithMessage(429, "rate_limited", "slow down", "retry-after", after);
    }

    private static TransportResponse boom() {
        return errorAnswer(500, "internal_error");
    }

    private static List<Duration> millis(long... values) {
        Duration[] result = new Duration[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Duration.ofMillis(values[i]);
        }
        return Arrays.asList(result);
    }

    // --- 429 -------------------------------------------------------------------

    @Test
    void status429WithRetryAfterWaitsThenSucceeds() {
        ApiResponse<MeData> response = builder(limited("3"), me()).build().getMe();
        assertEquals(200, response.getStatus());
        assertEquals(2, transport.requests.size());
        assertEquals(Collections.singletonList(Duration.ofSeconds(3)), sleeps);
    }

    @Test
    void theRetrySendsTheSameRequest() {
        builder(limited("1"), me()).build().getMe();
        assertEquals(transport.requests.get(0), transport.requests.get(1));
        assertEquals("Bearer " + TEST_KEY, transport.requests.get(1).headers.get("Authorization"));
    }

    @Test
    void status429WithoutRetryAfterIsNeverRetried() {
        PastClimateClient quota = builder(errorAnswer(429, "quota_exceeded")).build();
        QuotaExceededException e = assertThrows(QuotaExceededException.class, quota::getMe);
        assertEquals("quota_exceeded", e.getCode());
        assertEquals(1, transport.requests.size());
        assertTrue(sleeps.isEmpty());

        PastClimateClient limited = builder(errorAnswer(429, "rate_limited")).build();
        assertThrows(RateLimitedException.class, limited::getMe);
        assertEquals(1, transport.requests.size());
    }

    @Test
    void status429WithARetryAfterLongerThanTheCapIsThrownNotWaited() {
        PastClimateClient client =
                builder(errorAnswerWithMessage(429, "daily_request_cap_reached", "x", "retry-after", "3600")).build();
        RateLimitedException e = assertThrows(RateLimitedException.class, client::getMe);
        assertEquals(3600L, e.getRetryAfter());
        assertEquals(1, transport.requests.size());
        assertTrue(sleeps.isEmpty());
    }

    @Test
    void theCapIsConfigurable() {
        builder(limited("90"), me()).maxRetryDelay(Duration.ofSeconds(120)).build().getMe();
        assertEquals(Collections.singletonList(Duration.ofSeconds(90)), sleeps);
    }

    @Test
    void status429RetryAfterZeroRetriesAtOnce() {
        builder(limited("0"), me()).build().getMe();
        assertEquals(Collections.singletonList(Duration.ZERO), sleeps);
    }

    @Test
    void status429WithAnUnreadableRetryAfterIsNotRetried() {
        PastClimateClient client = builder(limited("soon")).build();
        assertThrows(RateLimitedException.class, client::getMe);
        assertEquals(1, transport.requests.size());
    }

    @Test
    void status429GivesUpAfterMaxRetries() {
        PastClimateClient client = builder(limited("1"), limited("1"), limited("1")).build();
        RateLimitedException e = assertThrows(RateLimitedException.class, client::getMe);
        assertEquals(1L, e.getRetryAfter());
        assertEquals(3, transport.requests.size()); // 1 try + 2 retries
        assertEquals(Arrays.asList(Duration.ofSeconds(1), Duration.ofSeconds(1)), sleeps);
    }

    // --- 5xx -------------------------------------------------------------------

    @Test
    void status5xxIsRetriedWithGrowingBackoff() {
        PastClimateClient client = builder(boom(), boom(), boom()).build();
        ServerException e = assertThrows(ServerException.class, client::getMe);
        assertEquals("internal_error", e.getCode());
        assertEquals(3, transport.requests.size());
        assertEquals(millis(500, 1000), sleeps);
    }

    @Test
    void backoffHasJitterBetweenHalfAndFull() {
        PastClimateClient client = builder(
                        errorAnswer(503, "api_disabled"), errorAnswer(503, "api_disabled"), errorAnswer(503, "api_disabled"))
                .random(() -> 0.0)
                .build();
        assertThrows(ServerException.class, client::getMe);
        assertEquals(millis(250, 500), sleeps);
    }

    @Test
    void backoffIsCappedByMaxRetryDelay() {
        PastClimateClient client = builder(boom(), boom(), boom(), boom())
                .maxRetries(3)
                .maxRetryDelay(Duration.ofMillis(600))
                .build();
        assertThrows(ServerException.class, client::getMe);
        assertEquals(millis(500, 600, 600), sleeps);
    }

    @Test
    void status5xxThenSuccess() {
        ApiResponse<MeData> response = builder(errorAnswer(503, "metering_unavailable"), me()).build().getMe();
        assertEquals(200, response.getStatus());
        assertEquals(2, transport.requests.size());
    }

    @Test
    void aStatus5xxWithoutAnErrorBodyIsRetried() {
        TransportResponse html = new TransportResponse(502, map(), "<html>bad gateway</html>");
        ApiResponse<MeData> response = builder(html, me()).build().getMe();
        assertEquals(200, response.getStatus());
    }

    @Test
    void maxRetriesZeroMeansOneTry() {
        PastClimateClient server = builder(boom()).maxRetries(0).build();
        assertThrows(ServerException.class, server::getMe);
        assertEquals(1, transport.requests.size());

        PastClimateClient limited = builder(limited("1")).maxRetries(0).build();
        assertThrows(RateLimitedException.class, limited::getMe);
        assertEquals(1, transport.requests.size());
    }

    @Test
    void theDefaultIsTwoRetries() {
        PastClimateClient client = builder(boom(), boom(), boom(), boom()).build();
        assertThrows(ServerException.class, client::getMe);
        assertEquals(3, transport.requests.size());
    }

    // --- other 4xx are never retried -------------------------------------------

    @Test
    void otherClientErrorsAreNeverRetried() {
        Object[][] cases = {
            {400, "invalid_request"}, {400, "range_too_large"}, {401, "invalid_key"}, {403, "plan_required"},
            {404, "location_not_found"}, {404, "not_published"}, {422, "location_blocked"},
        };
        for (Object[] row : cases) {
            String code = (String) row[1];
            PastClimateClient client = builder(errorAnswer((Integer) row[0], code), me()).build();
            assertThrows(ApiException.class, client::getMe, code);
            assertEquals(1, transport.requests.size(), code);
            assertTrue(sleeps.isEmpty(), code);
        }
    }

    // --- network errors --------------------------------------------------------

    @Test
    void networkErrorsAreRetried() {
        PastClimateClient client =
                builder(new ConnectionException("reset"), new ConnectionException("reset"), me()).build();
        ApiResponse<MeData> response = client.getMe();
        assertEquals(200, response.getStatus());
        assertEquals(3, transport.requests.size());
        assertEquals(millis(500, 1000), sleeps);
    }

    @Test
    void networkErrorsAreThrownWhenRetriesRunOut() {
        PastClimateClient client = builder(
                        new ConnectionException("timed out", true, null),
                        new ConnectionException("timed out", true, null),
                        new ConnectionException("timed out", true, null))
                .build();
        ConnectionException e = assertThrows(ConnectionException.class, client::getMe);
        assertTrue(e.isTimeout());
        assertEquals(3, transport.requests.size());
    }

    @Test
    void aNonTimeoutConnectionError() {
        PastClimateClient client = builder(new ConnectionException("refused")).maxRetries(0).build();
        ConnectionException e = assertThrows(ConnectionException.class, client::getMe);
        assertFalse(e.isTimeout());
    }

    @Test
    void csvRequestsRetryToo() {
        PastClimateClient client = builder(boom(), ok(fixture("daily.csv"))).build();
        CsvResponse response = client.getDailyCsv(Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2));
        assertEquals(fixture("daily.csv"), response.getCsv());
        assertEquals(1, sleeps.size());
    }

    @Test
    void notFoundIsNotRetried() {
        PastClimateClient client = builder(errorAnswer(404, "not_published")).build();
        assertThrows(NotFoundException.class, client::getMe);
        assertEquals(1, transport.requests.size());
    }
}
