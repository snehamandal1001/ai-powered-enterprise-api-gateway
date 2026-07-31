package com.aigateway.order.dto;

import com.aigateway.order.model.Order;
import com.aigateway.order.model.OrderItem;
import com.aigateway.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemView> items;
    private Instant createdAt;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.customerName = order.getCustomerName();
        response.status = order.getStatus();
        response.totalAmount = order.getTotalAmount();
        response.createdAt = order.getCreatedAt();
        response.items = order.getItems().stream()
                .map(OrderItemView::fromEntity)
                .toList();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemView> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class OrderItemView {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public static OrderItemView fromEntity(OrderItem item) {
            OrderItemView view = new OrderItemView();
            view.productId = item.getProductId();
            view.productName = item.getProductName();
            view.quantity = item.getQuantity();
            view.unitPrice = item.getUnitPrice();
            return view;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
    }
}
