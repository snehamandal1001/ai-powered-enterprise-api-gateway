package com.aigateway.analytics.dto;

import java.util.List;
import java.util.Map;

public class SummaryResponse {

    private long totalRequests;
    private Double averageDurationMs;
    private List<Map<String, Object>> requestsByPath;
    private List<Map<String, Object>> errorsByPath;
    private String windowDescription;

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Double getAverageDurationMs() {
        return averageDurationMs;
    }

    public void setAverageDurationMs(Double averageDurationMs) {
        this.averageDurationMs = averageDurationMs;
    }

    public List<Map<String, Object>> getRequestsByPath() {
        return requestsByPath;
    }

    public void setRequestsByPath(List<Map<String, Object>> requestsByPath) {
        this.requestsByPath = requestsByPath;
    }

    public List<Map<String, Object>> getErrorsByPath() {
        return errorsByPath;
    }

    public void setErrorsByPath(List<Map<String, Object>> errorsByPath) {
        this.errorsByPath = errorsByPath;
    }

    public String getWindowDescription() {
        return windowDescription;
    }

    public void setWindowDescription(String windowDescription) {
        this.windowDescription = windowDescription;
    }
}
