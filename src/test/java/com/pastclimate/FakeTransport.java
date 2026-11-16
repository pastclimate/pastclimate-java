package com.pastclimate;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A transport that never touches the network. Queue answers (a TransportResponse, or a RuntimeException
 * to throw) and read {@code requests} afterwards.
 */
final class FakeTransport implements HttpTransport {
    static final class Request {
        final URI uri;
        final Map<String, String> headers;
        final Duration timeout;

        Request(URI uri, Map<String, String> headers, Duration timeout) {
            this.uri = uri;
            this.headers = headers;
            this.timeout = timeout;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Request)) {
                return false;
            }
            Request that = (Request) other;
            return uri.equals(that.uri) && headers.equals(that.headers) && timeout.equals(that.timeout);
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }
    }

    final List<Request> requests = new ArrayList<>();
    private final Deque<Object> answers = new ArrayDeque<>();

    FakeTransport(Object... answers) {
        this.answers.addAll(Arrays.asList(answers));
    }

    @Override
    public TransportResponse get(URI uri, Map<String, String> headers, Duration timeout) {
        requests.add(new Request(uri, new LinkedHashMap<>(headers), timeout));
        Object next = answers.poll();
        if (next == null) {
            throw new IllegalStateException(
                    "FakeTransport: request #" + requests.size() + " had no answer queued (" + uri + ")");
        }
        if (next instanceof RuntimeException) {
            throw (RuntimeException) next;
        }
        return (TransportResponse) next;
    }

    Request last() {
        return requests.get(requests.size() - 1);
    }
}
