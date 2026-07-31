package com.aigateway.order.service;

import com.aigateway.order.client.CatalogClient;
import com.aigateway.order.client.ProductDto;
import com.aigateway.order.dto.OrderItemRequest;
import com.aigateway.order.dto.OrderRequest;
import com.aigateway.order.dto.OrderResponse;
import com.aigateway.order.exception.InsufficientStockException;
import com.aigateway.order.model.Order;
import com.aigateway.order.model.OrderStatus;
import com.aigateway.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * These tests mock CatalogClient entirely, so they run instantly and
 * don't need catalog-service, Docker, or a real database running.
 * This is exactly how "unit testing framework" experience looks on a resume.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest sampleRequest;
    private ProductDto sampleProduct;

    @BeforeEach
    void setUp() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        sampleRequest = new OrderRequest();
        sampleRequest.setCustomerName("Sneha Mandal");
        sampleRequest.setItems(List.of(itemRequest));

        sampleProduct = new ProductDto();
        sampleProduct.setId(1L);
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setPrice(new BigDecimal("19.99"));
        sampleProduct.setStockQuantity(100);
    }

    @Test
    void placeOrder_confirmsOrder_whenStockIsAvailable() {
        when(catalogClient.getProduct(1L)).thenReturn(sampleProduct);
        when(catalogClient.reserveStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(sampleRequest);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("39.98"), response.getTotalAmount());
        verify(catalogClient, times(1)).reserveStock(1L, 2);
    }

    @Test
    void placeOrder_throwsException_whenStockIsInsufficient() {
        when(catalogClient.getProduct(1L)).thenReturn(sampleProduct);
        when(catalogClient.reserveStock(1L, 2)).thenReturn(false);

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(sampleRequest));

        verify(orderRepository, never()).save(any(Order.class));
    }
}
