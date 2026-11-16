package com.pastclimate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pastclimate.model.MeData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Talks to a throwaway HTTP server on 127.0.0.1, so the default java.net.http transport is exercised for
 * real (headers, query string, status, redirects, timeouts).
 */
class JdkHttpTransportTest extends TestSupport {
    /** What the handler answers. */
    private static final class Reply {
        final int status;
        final String contentType;
        final String body;
        final String[] extraHeaders;

        Reply(int status, String contentType, String body, String... extraHeaders) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
            this.extraHeaders = extraHeaders;
        }
    }

    private interface Handler {
        Reply reply(int callNumber) throws InterruptedException;
    }

    private HttpServer server;
    private ExecutorService executor;
    private final List<String> requestLines = new CopyOnWriteArrayList<>();
    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private final List<String> userAgents = new CopyOnWriteArrayList<>();

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private String start(Handler handler) throws IOException {
        AtomicInteger calls = new AtomicInteger();
        executor = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            requestLines.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getRawPath()
                    + (exchange.getRequestURI().getRawQuery() == null ? "" : "?" + exchange.getRequestURI().getRawQuery()));
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            userAgents.add(exchange.getRequestHeaders().getFirst("User-Agent"));
            try {
                respond(exchange, handler.reply(calls.incrementAndGet()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, Reply reply) throws IOException {
        byte[] bytes = reply.body.getBytes(StandardCharsets.UTF_8);
        if (reply.contentType != null) {
            exchange.getResponseHeaders().add("Content-Type", reply.contentType);
        }
        for (int i = 0; i < reply.extraHeaders.length; i += 2) {
            exchange.getResponseHeaders().add(reply.extraHeaders[i], reply.extraHeaders[i + 1]);
        }
        if (bytes.length == 0) {
            exchange.sendResponseHeaders(reply.status, -1);
        } else {
            exchange.sendResponseHeaders(reply.status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
        exchange.close();
    }

    private static String errorBody(String code) {
        return "{\"error\":{\"code\":\"" + code + "\",\"message\":\"m\",\"docs_url\":\"https://example.test/e\"}}";
    }

    private PastClimateClient realClient(String baseUrl) {
        return PastClimateClient.builder().apiKey(TEST_KEY).baseUrl(baseUrl).build();
    }

    @Test
    void aRealRoundTrip() throws IOException {
        String base = start(n -> new Reply(200, "application/json", fixture("daily.json"),
                "RateLimit-Remaining", "16", "X-Credits-Used", "7"));
        ApiResponse<List<com.pastclimate.model.DailyRow>> response = realClient(base)
                .getDaily(Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), Units.IMPERIAL);

        assertEquals(1, requestLines.size());
        assertTrue(requestLines.get(0).startsWith("GET /v1/daily?"), requestLines.get(0));
        assertTrue(requestLines.get(0).contains("location=gb%2Flondon"), requestLines.get(0));
        assertTrue(requestLines.get(0).contains("units=imperial"), requestLines.get(0));
        assertFalse(requestLines.get(0).contains(TEST_KEY));
        assertEquals("Bearer " + TEST_KEY, authorizations.get(0));
        assertEquals("pastclimate-java/" + Version.VERSION, userAgents.get(0));

        assertEquals(2, response.getData().size());
        assertEquals(7L, response.getMeta().getCredits());
        assertEquals(16, response.getRateLimit().getRemaining());
        assertEquals(7L, response.getCreditsUsed());
    }

    @Test
    void csvOverARealSocket() throws IOException {
        String base = start(n -> new Reply(200, "text/csv", fixture("daily.csv"), "X-Credits-Used", "7"));
        CsvResponse response = realClient(base)
                .getDailyCsv(Place.id("gb/london"), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2));
        assertEquals(fixture("daily.csv"), response.getCsv());
        assertEquals(7L, response.getCreditsUsed());
        assertTrue(requestLines.get(0).contains("format=csv"));
    }

    @Test
    void anErrorAnswerIsTyped() throws IOException {
        String base = start(n -> new Reply(404, "application/json", errorBody("location_not_found")));
        PastClimateClient client = realClient(base);
        NotFoundException e = assertThrows(NotFoundException.class, () -> client.getLocation("gb/nowhere"));
        assertEquals("location_not_found", e.getCode());
        assertEquals(1, requestLines.size());
    }

    @Test
    void status429WithRetryAfterIsRetriedForReal() throws IOException {
        String base = start(n -> n == 1
                ? new Reply(429, "application/json", errorBody("rate_limited"), "Retry-After", "0")
                : new Reply(200, "application/json", fixture("me.json")));
        ApiResponse<MeData> response = realClient(base).getMe();
        assertEquals(200, response.getStatus());
        assertEquals(2, requestLines.size());
    }

    @Test
    void status429WithoutRetryAfterIsNotRetriedForReal() throws IOException {
        String base = start(n -> new Reply(429, "application/json", errorBody("quota_exceeded")));
        PastClimateClient client = realClient(base);
        assertThrows(QuotaExceededException.class, client::getMe);
        assertEquals(1, requestLines.size());
    }

    @Test
    void redirectsAreNotFollowedSoTheKeyCannotLeak() throws IOException {
        String base = start(n -> new Reply(302, null, "", "Location", "http://example.invalid/steal"));
        PastClimateClient client = realClient(base);
        ApiException e = assertThrows(ApiException.class, client::getMe);
        assertEquals(302, e.getStatus());
        assertEquals("http_302", e.getCode());
        assertEquals(1, requestLines.size());
    }

    @Test
    void aTimeoutIsAConnectionException() throws IOException {
        String base = start(n -> {
            Thread.sleep(2000);
            return new Reply(200, "application/json", fixture("me.json"));
        });
        PastClimateClient client = PastClimateClient.builder()
                .apiKey(TEST_KEY).baseUrl(base).timeout(Duration.ofMillis(300)).maxRetries(0).build();
        ConnectionException e = assertThrows(ConnectionException.class, client::getMe);
        assertTrue(e.isTimeout());
    }

    @Test
    void aRefusedConnectionIsAConnectionException() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
            port = socket.getLocalPort();
        }
        PastClimateClient client = PastClimateClient.builder()
                .apiKey(TEST_KEY).baseUrl("http://127.0.0.1:" + port).maxRetries(0).build();
        ConnectionException e = assertThrows(ConnectionException.class, client::getMe);
        assertFalse(e.isTimeout());
    }
}
