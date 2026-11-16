package com.pastclimate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;

/** Maps an HTTP error answer to one of the exception classes. */
final class ErrorMapper {
    /** Builds one exception class. */
    private interface Factory {
        ApiException create(
                String message, int status, String code, String docsUrl, Map<String, String> headers, String body);
    }

    private static final Map<String, Factory> CODE_CLASSES = new HashMap<>();

    static {
        Factory auth = AuthenticationException::new;
        Factory invalid = InvalidRequestException::new;
        Factory notFound = NotFoundException::new;
        Factory rateLimited = RateLimitedException::new;
        Factory quota = QuotaExceededException::new;
        Factory server = ServerException::new;
        CODE_CLASSES.put("missing_key", auth);
        CODE_CLASSES.put("invalid_key", auth);
        CODE_CLASSES.put("key_revoked", auth);
        CODE_CLASSES.put("email_not_verified", auth);
        CODE_CLASSES.put("account_suspended", auth);
        CODE_CLASSES.put("plan_required", PlanRequiredException::new);
        CODE_CLASSES.put("invalid_request", invalid);
        CODE_CLASSES.put("range_too_large", invalid);
        CODE_CLASSES.put("key_in_query", invalid);
        CODE_CLASSES.put("cors_not_supported", invalid);
        CODE_CLASSES.put("method_not_allowed", invalid);
        CODE_CLASSES.put("location_not_found", notFound);
        CODE_CLASSES.put("not_published", notFound);
        CODE_CLASSES.put("not_found", notFound);
        CODE_CLASSES.put("location_blocked", notFound);
        CODE_CLASSES.put("rate_limited", rateLimited);
        CODE_CLASSES.put("daily_request_cap_reached", rateLimited);
        CODE_CLASSES.put("quota_exceeded", quota);
        CODE_CLASSES.put("daily_cap_reached", quota);
        CODE_CLASSES.put("internal_error", server);
        CODE_CLASSES.put("api_disabled", server);
        CODE_CLASSES.put("metering_unavailable", server);
    }

    private ErrorMapper() {
    }

    /** Builds (does not throw) the exception for a non-2xx answer. */
    static ApiException fromResponse(TransportResponse response) {
        int status = response.getStatus();
        String code = null;
        String message = null;
        String docsUrl = null;
        try {
            JsonElement root = JsonParser.parseString(response.getBody());
            if (root.isJsonObject()) {
                JsonElement error = root.getAsJsonObject().get("error");
                if (error != null && error.isJsonObject()) {
                    JsonObject detail = error.getAsJsonObject();
                    code = text(detail, "code");
                    message = text(detail, "message");
                    docsUrl = text(detail, "docs_url");
                }
            }
        } catch (RuntimeException ignored) {
            code = null;
        }

        if (code == null) {
            code = "http_" + status;
            message = "The API answered HTTP " + status + " without an error body.";
            docsUrl = null;
        } else if (message == null) {
            message = "The PastClimate API returned an error (" + code + ").";
        }

        Factory factory = CODE_CLASSES.get(code);
        if (factory == null) {
            factory = classForStatus(status);
        }
        return factory.create(message, status, code, docsUrl, response.getHeaders(), response.getBody());
    }

    private static Factory classForStatus(int status) {
        if (status == 401) {
            return AuthenticationException::new;
        }
        if (status == 400 || status == 405) {
            return InvalidRequestException::new;
        }
        if (status == 404) {
            return NotFoundException::new;
        }
        if (status == 429) {
            return RateLimitedException::new;
        }
        if (status >= 500 && status <= 599) {
            return ServerException::new;
        }
        return ApiException::new;
    }

    private static String text(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }
}
