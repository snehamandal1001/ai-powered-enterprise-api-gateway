package com.aigateway.analytics.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "request_logs")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Long durationMs;

    @Column(nullable = false)
    private String clientIp;

    @Column(nullable = false)
    private Instant occurredAt;

    public RequestLog() {
    }

    public RequestLog(String httpMethod, String path, Integer statusCode, Long durationMs, String clientIp) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.statusCode = statusCode;
        this.durationMs = durationMs;
        this.clientIp = clientIp;
    }

    @PrePersist
    protected void onCreate() {
        occurredAt = Instant.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
