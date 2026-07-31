package com.aigateway.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * This class is the "phone line" from order-service to catalog-service.
 * Every method here makes a real HTTP call across the network (well,
 * across localhost for now) to the other microservice.
 */
@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String catalogServiceBaseUrl;

    public CatalogClient(RestTemplate restTemplate,
                          @Value("${catalog.service.url:http://localhost:8081}") String catalogServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.catalogServiceBaseUrl = catalogServiceBaseUrl;
    }

    public ProductDto getProduct(Long productId) {
        String url = catalogServiceBaseUrl + "/api/products/" + productId;
        try {
            return restTemplate.getForObject(url, ProductDto.class);
        } catch (RestClientException ex) {
            throw new CatalogServiceUnavailableException(
                    "Could not fetch product " + productId + " from catalog-service", ex);
        }
    }

    /**
     * Calls catalog-service's /reserve endpoint to attempt to decrement stock.
     * Returns true if there was enough stock and it was reserved successfully.
     */
    public boolean reserveStock(Long productId, int quantity) {
        String url = catalogServiceBaseUrl + "/api/products/" + productId + "/reserve?quantity=" + quantity;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> response = restTemplate.postForObject(url, null, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("reserved"));
        } catch (RestClientException ex) {
            throw new CatalogServiceUnavailableException(
                    "Could not reserve stock for product " + productId + " via catalog-service", ex);
        }
    }
}
