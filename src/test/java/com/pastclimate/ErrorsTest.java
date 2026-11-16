package com.pastclimate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

class ErrorsTest extends TestSupport {
    private static final Object[][] EXPECTED = {
        {"missing_key", 401, AuthenticationException.class},
        {"invalid_key", 401, AuthenticationException.class},
        {"key_revoked", 401, AuthenticationException.class},
        {"email_not_verified", 403, AuthenticationException.class},
        {"account_suspended", 403, AuthenticationException.class},
        {"plan_required", 403, PlanRequiredException.class},
        {"cors_not_supported", 403, InvalidRequestException.class},
        {"invalid_request", 400, InvalidRequestException.class},
        {"range_too_large", 400, InvalidRequestException.class},
        {"key_in_query", 400, InvalidRequestException.class},
        {"method_not_allowed", 405, InvalidRequestException.class},
        {"location_not_found", 404, NotFoundException.class},
        {"not_published", 404, NotFoundException.class},
        {"not_found", 404, NotFoundException.class},
        {"location_blocked", 422, NotFoundException.class},
        {"rate_limited", 429, RateLimitedException.class},
        {"daily_request_cap_reached", 429, RateLimitedException.class},
        {"quota_exceeded", 429, QuotaExceededException.class},
        {"internal_error", 500, ServerException.class},
        {"api_disabled", 503, ServerException.class},
        {"metering_unavailable", 503, ServerException.class},
    };

    private ApiException catchError(TransportResponse answer) {
        PastClimateClient client = builder(answer).maxRetries(0).build();
        return assertThrows(ApiException.class, client::getMe);
    }

    @Test
    void everyDocumentedCodeMapsToItsClass() {
        for (Object[] row : EXPECTED) {
            String code = (String) row[0];
            int status = (Integer) row[1];
            Class<?> expectedClass = (Class<?>) row[2];

            ApiException e = catchError(errorAnswerWithMessage(status, code, "msg for " + code));
            assertEquals(expectedClass, e.getClass(), code);
            assertEquals(code, e.getCode());
            assertEquals(status, e.getStatus());
            assertEquals("msg for " + code, e.getMessage());
            assertEquals("https://developer.pastclimate.com/api/getting-started/errors/#" + code, e.getDocsUrl());
            assertTrue(e instanceof PastClimateException);
        }
    }

    @Test
    void theExceptionCarriesHeadersRateLimitAndRetryInfo() {
        ApiException e = catchError(errorAnswerWithMessage(429, "rate_limited", "slow down",
                "retry-after", "12",
                "x-upgrade-url", "https://example.test/upgrade",
                "ratelimit-limit", "17",
                "ratelimit-remaining", "0",
                "ratelimit-reset", "12"));
        assertTrue(e instanceof RateLimitedException);
        assertEquals(12L, e.getRetryAfter());
        assertEquals("https://example.test/upgrade", e.getUpgradeUrl());
        assertEquals(new RateLimit(17, 0, 12), e.getRateLimit());
        assertEquals("12", e.getHeaders().get("retry-after"));
        assertTrue(e.getBody().contains("rate_limited"));
    }

    @Test
    void quotaExceededWithoutRetryAfter() {
        ApiException e = catchError(errorAnswer(429, "quota_exceeded"));
        assertTrue(e instanceof QuotaExceededException);
        assertNull(e.getRetryAfter());
        assertNull(e.getUpgradeUrl());
    }

    @Test
    void aBodyThatIsNotTheApiErrorEnvelope() {
        ApiException e = catchError(
                new TransportResponse(502, map("content-type", "text/html"), "<html>Bad gateway</html>"));
        assertTrue(e instanceof ServerException);
        assertEquals("http_502", e.getCode());
        assertEquals(502, e.getStatus());
        assertNull(e.getDocsUrl());
        assertTrue(e.getMessage().contains("502"));
    }

    @Test
    void anUnknownCodeFallsBackToTheStatus() {
        Object[][] cases = {
            {401, AuthenticationException.class},
            {400, InvalidRequestException.class},
            {404, NotFoundException.class},
            {429, RateLimitedException.class},
            {500, ServerException.class},
            {504, ServerException.class},
            {403, ApiException.class},
            {418, ApiException.class},
            {302, ApiException.class},
        };
        for (Object[] row : cases) {
            int status = (Integer) row[0];
            ApiException e = catchError(errorAnswer(status, "a_code_from_the_future"));
            assertEquals(row[1], e.getClass(), "status " + status);
            assertEquals("a_code_from_the_future", e.getCode());
        }
    }

    @Test
    void everyClassIsAnApiException() {
        Class<?>[] classes = {
            AuthenticationException.class, InvalidRequestException.class, NotFoundException.class,
            PlanRequiredException.class, RateLimitedException.class, QuotaExceededException.class,
            ServerException.class,
        };
        for (Class<?> c : classes) {
            assertTrue(ApiException.class.isAssignableFrom(c), c.getName());
        }
        assertTrue(PastClimateException.class.isAssignableFrom(ApiException.class));
        assertTrue(PastClimateException.class.isAssignableFrom(ConnectionException.class));
        assertTrue(PastClimateException.class.isAssignableFrom(ConfigurationException.class));
        assertFalse(ApiException.class.isAssignableFrom(ConnectionException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(PastClimateException.class));
    }

    @Test
    void errorsCanBeCaughtByCategory() {
        PastClimateClient client = builder(errorAnswer(404, "location_not_found")).build();
        String caught = null;
        try {
            client.getMe();
        } catch (NotFoundException e) {
            caught = e.getCode();
        }
        assertEquals("location_not_found", caught);
    }

    @Test
    void headersGivenInAnyCaseAreReadable() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Retry-After", "5");
        headers.put("X-Upgrade-URL", "https://example.test/u");
        ApiException e = new ApiException("m", 429, "rate_limited", null, headers, "");
        assertEquals(5L, e.getRetryAfter());
        assertEquals("https://example.test/u", e.getUpgradeUrl());
    }
}
