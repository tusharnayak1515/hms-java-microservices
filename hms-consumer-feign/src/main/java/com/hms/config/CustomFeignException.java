package com.hms.config;

public class CustomFeignException extends RuntimeException {

    private final int statusCode;
    private final String errorMessage;

    public CustomFeignException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}