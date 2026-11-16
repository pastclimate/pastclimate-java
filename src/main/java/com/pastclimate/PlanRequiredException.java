package com.pastclimate;

import java.util.Map;

/**
 * Your plan does not include this request. Code: plan_required.
 */
public class PlanRequiredException extends ApiException {
    private static final long serialVersionUID = 1L;

    public PlanRequiredException(
            String message, int status, String code, String docsUrl, Map<String, String> headers, String body) {
        super(message, status, code, docsUrl, headers, body);
    }
}
