package com.aigateway.gateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Spring Cloud Gateway's rate limiter needs to know WHO to count
 * requests against - otherwise "10 requests per second" would mean
 * 10 requests total for every single user combined, which isn't
 * useful. This class says: count requests per client IP address.
 *
 * (A more advanced version would key by the logged-in username
 * instead of IP - a natural next improvement once you're comfortable
 * with this version.)
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }
}
