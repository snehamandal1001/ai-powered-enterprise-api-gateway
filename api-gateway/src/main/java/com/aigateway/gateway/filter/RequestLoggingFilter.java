package com.aigateway.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Runs for EVERY request that passes through the gateway, no matter
 * which service it's headed to. For now it just logs the request.
 * In Step 7, this is where we'll send request data to analytics-service
 * instead of just printing it.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        log.info("Incoming request: {} {}", method, path);

        return chain.filter(exchange).doFinally(signalType -> {
            long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;
            log.info("Completed request: {} {} -> status={} took={}ms", method, path, statusCode, durationMs);
        });
    }

    @Override
    public int getOrder() {
        return -1; // run early, before routing
    }
}
