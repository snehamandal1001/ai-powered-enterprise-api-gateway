package com.aigateway.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /**
     * RestTemplate is how this service makes HTTP calls to catalog-service.
     * Think of it as the "phone" order-service uses to call catalog-service.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
