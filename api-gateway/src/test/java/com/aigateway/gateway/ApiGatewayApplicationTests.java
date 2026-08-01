package com.aigateway.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A minimal test that just confirms the gateway's Spring context
 * starts up correctly with the routes configured in application.yml.
 */
@SpringBootTest
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, it means Spring Boot successfully
        // read application.yml and built the gateway routes without errors.
    }
}
