package com.aigateway.order.client;

public class CatalogServiceUnavailableException extends RuntimeException {
    public CatalogServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
