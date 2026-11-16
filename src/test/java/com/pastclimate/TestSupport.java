package com.pastclimate;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Helpers shared by the tests. */
abstract class TestSupport {
    /** A made-up key. It is not a real PastClimate key. */
    static final String TEST_KEY = "test-key-0123456789";

    FakeTransport transport;
    final List<Duration> sleeps = new ArrayList<>();

    static String fixture(String name) {
        try (InputStream in = TestSupport.class.getResourceAsStream("/fixtures/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing fixture " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Builds a map from alternating names and values. */
    static Map<String, String> map(String... pairs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(pairs[i], pairs[i + 1]);
        }
        return result;
    }

    static TransportResponse ok(String body, String... extraHeaders) {
        Map<String, String> headers = map(
                "content-type", "application/json",
                "ratelimit-limit", "17",
                "ratelimit-remaining", "16",
                "ratelimit-reset", "42",
                "x-credits-used", "7",
                "x-credits-remaining", "1234");
        headers.putAll(map(extraHeaders));
        return new TransportResponse(200, headers, body);
    }

    static TransportResponse okFixture(String name) {
        return ok(fixture(name));
    }

    static TransportResponse errorAnswer(int status, String code, String... extraHeaders) {
        return errorAnswerWithMessage(status, code, "Something is wrong.", extraHeaders);
    }

    static TransportResponse errorAnswerWithMessage(int status, String code, String message, String... extraHeaders) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("code", code);
        detail.put("message", message);
        detail.put("docs_url", "https://developer.pastclimate.com/api/getting-started/errors/#" + code);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", detail);
        Map<String, String> headers = map("content-type", "application/json");
        headers.putAll(map(extraHeaders));
        return new TransportResponse(status, headers, new Gson().toJson(body));
    }

    /** A builder wired to a fake transport with a recording sleeper (see {@code sleeps}). */
    PastClimateClient.Builder builder(Object... answers) {
        transport = new FakeTransport(answers);
        sleeps.clear();
        return PastClimateClient.builder()
                .apiKey(TEST_KEY)
                .transport(transport)
                .sleeper(sleeps::add)
                .random(() -> 1.0);
    }

    PastClimateClient client(Object... answers) {
        return builder(answers).build();
    }

    /** The decoded query parameters of a URI. */
    static Map<String, String> query(URI uri) {
        Map<String, String> result = new LinkedHashMap<>();
        String raw = uri.getRawQuery();
        if (raw == null) {
            return result;
        }
        for (String pair : raw.split("&")) {
            int at = pair.indexOf('=');
            String name = at < 0 ? pair : pair.substring(0, at);
            String value = at < 0 ? "" : pair.substring(at + 1);
            result.put(URLDecoder.decode(name, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return result;
    }
}
