package com.hms.dto;

public class CustomErrorResponse {
    private int statusCode;
    private boolean success;
	private String error;

    public CustomErrorResponse() {
        super();
    }

    public CustomErrorResponse(int statusCode, boolean success, String error) {
        this.statusCode = statusCode;
        this.success = success;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CustomErrorResponse [statusCode=" + statusCode + ", success=" + success + ", error=" + error + "]";
    }

    
}
