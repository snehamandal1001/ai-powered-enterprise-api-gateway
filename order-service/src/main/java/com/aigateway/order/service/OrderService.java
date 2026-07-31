package com.aigateway.order.service;

import com.aigateway.order.client.CatalogClient;
import com.aigateway.order.client.ProductDto;
import com.aigateway.order.dto.OrderItemRequest;
import com.aigateway.order.dto.OrderRequest;
import com.aigateway.order.dto.OrderResponse;
import com.aigateway.order.exception.InsufficientStockException;
import com.aigateway.order.exception.OrderNotFoundException;
import com.aigateway.order.model.Order;
import com.aigateway.order.model.OrderItem;
import com.aigateway.order.model.OrderStatus;
import com.aigateway.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderService(OrderRepository orderRepository, CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    /**
     * Places a new order. For every item in the request:
     *   1. Ask catalog-service what the product looks like (price, name).
     *   2. Ask catalog-service to reserve the requested quantity.
     *   3. If any reservation fails, the whole order is rejected.
     * This is the moment order-service actually "talks" to catalog-service
     * over the network.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Order order = new Order(request.getCustomerName());
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = catalogClient.getProduct(itemRequest.getProductId());

            boolean reserved = catalogClient.reserveStock(
                    itemRequest.getProductId(), itemRequest.getQuantity());

            if (!reserved) {
                throw new InsufficientStockException(itemRequest.getProductId());
            }

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    itemRequest.getQuantity(),
                    product.getPrice()
            );
            order.addItem(orderItem);
            total = total.add(orderItem.getLineTotal());
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }
}
