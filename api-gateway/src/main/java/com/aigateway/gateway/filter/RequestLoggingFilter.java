package com.aigateway.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Runs for EVERY request that passes through the gateway. It logs to
 * the console AND fires off the same information to analytics-service,
 * so every request in the whole system ends up recorded there too.
 *
 * The call to analytics-service is "fire and forget" - we don't make
 * the actual user's request wait for it, and if analytics-service is
 * down for any reason, it must never break the real request.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final WebClient webClient;

    public RequestLoggingFilter(
            WebClient.Builder webClientBuilder,
            @Value("${analytics.service.url:http://localhost:8083}") String analyticsServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(analyticsServiceUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        return chain.filter(exchange).doFinally(signalType -> {
            long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            log.info("Completed request: {} {} -> status={} took={}ms", method, path, statusCode, durationMs);

            sendToAnalytics(method, path, statusCode, durationMs, clientIp);
        });
    }

    private void sendToAnalytics(String method, String path, int statusCode, long durationMs, String clientIp) {
        Map<String, Object> body = Map.of(
                "httpMethod", method,
                "path", path,
                "statusCode", statusCode,
                "durationMs", durationMs,
                "clientIp", clientIp
        );

        webClient.post()
                .uri("/api/analytics/logs")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> { /* success - nothing more to do */ },
                        error -> log.warn("Could not send request log to analytics-service: {}", error.getMessage())
                );
    }

    @Override
    public int getOrder() {
        return -1; // run early, before routing
    }
}
