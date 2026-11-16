package com.pastclimate;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * Sends one GET request. The default is built on {@code java.net.http.HttpClient}. Implement this to use
 * another HTTP library, or a fake in tests.
 */
public interface HttpTransport {
    /**
     * @param uri the full URL
     * @param headers request headers (name to value)
     * @param timeout how long to wait for the answer
     * @return the answer, whatever its status
     * @throws ConnectionException when no HTTP answer arrives (DNS, TLS, reset, timeout)
     */
    TransportResponse get(URI uri, Map<String, String> headers, Duration timeout);
}
