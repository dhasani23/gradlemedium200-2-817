package com.gradlemedium200.orderservice.dto;

import com.gradlemedium200.orderservice.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    
    private String orderId;
    private String customerId;
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    
    /**
     * Sets the order items
     * 
     * @param items The order items
     */
    public void setOrderItems(List<OrderItemDto> items) {
        this.items = items;
    }
    
    /**
     * Sets the order status
     * 
     * @param status The order status
     */
    public void setOrderStatus(String status) {
        this.status = status;
    }
    
    /**
     * Converts an Order entity to an OrderResponseDto.
     * 
     * @param order The order entity to convert
     * @return The OrderResponseDto
     */
    public static OrderResponseDto fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderItemDto> itemDtos = new ArrayList<>();
        if (order.getOrderItems() != null) {
            itemDtos = order.getOrderItems().stream()
                .map(item -> new OrderItemDto(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        }
        
        String statusName = null;
        if (order.getStatus() != null) {
            statusName = order.getStatus();
        } else if (order.getOrderStatus() != null) {
            statusName = order.getOrderStatus().name();
        }
        
        return OrderResponseDto.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .status(statusName)
            .createdAt(order.getCreatedAt())
            .totalAmount(order.getTotalAmount())
            .items(itemDtos)
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .paymentMethod(order.getPaymentMethod())
            .build();
    }
}