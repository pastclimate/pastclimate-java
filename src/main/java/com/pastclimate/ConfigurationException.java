package com.pastclimate;

/** The client was set up wrongly (for example, no API key). */
public class ConfigurationException extends PastClimateException {
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }
}
