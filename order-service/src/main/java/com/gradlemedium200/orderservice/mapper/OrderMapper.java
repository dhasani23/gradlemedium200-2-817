package com.gradlemedium200.orderservice.mapper;

import com.gradlemedium200.orderservice.dto.OrderItemDto;
import com.gradlemedium200.orderservice.dto.OrderRequestDto;
import com.gradlemedium200.orderservice.dto.OrderResponseDto;
import com.gradlemedium200.orderservice.model.Order;
import com.gradlemedium200.orderservice.model.OrderItem;
import com.gradlemedium200.orderservice.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between order entities and DTOs.
 * This class handles the transformation of domain models to data transfer objects and vice versa.
 */
public class OrderMapper {

    /**
     * Converts an order entity to a response DTO.
     *
     * @param order The order entity to convert
     * @return The OrderResponseDto containing the order's data
     */
    public OrderResponseDto toResponseDto(Order order) {
        if (order == null) {
            return null;
        }
        
        // Convert OrderItems to OrderItemDtos
        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::mapOrderItemToDto)
                .collect(Collectors.toList());
        
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setOrderId(order.getOrderId());
        responseDto.setCustomerId(order.getCustomerId());
        responseDto.setOrderItems(itemDtos);
        responseDto.setOrderStatus(order.getOrderStatus().name());
        responseDto.setTotalAmount(order.getTotalAmount());
        responseDto.setCreatedAt(order.getCreatedAt());
        responseDto.setShippingAddress(order.getShippingAddress());
        
        return responseDto;
    }

    /**
     * Converts an order request DTO to an entity.
     *
     * @param orderRequest The order request DTO to convert
     * @return The Order entity populated with data from the request
     */
    public Order toEntity(OrderRequestDto orderRequest) {
        if (orderRequest == null) {
            return null;
        }
        
        // Create new order with generated ID
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setCustomerId(orderRequest.getCustomerId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Convert OrderItemDto list to OrderItem list
        if (orderRequest.getOrderItems() != null) {
            List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                    .map(this::mapDtoToOrderItem)
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);
        }
        
        // Calculate the total amount based on order items
        order.calculateTotalAmount();
        
        // TODO: Add payment processing information when payment service is integrated
        
        return order;
    }

    /**
     * Converts a list of order entities to a list of response DTOs.
     *
     * @param orders The list of order entities to convert
     * @return A list of OrderResponseDto objects
     */
    public List<OrderResponseDto> toResponseDtoList(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        
        return orders.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps an OrderItem entity to an OrderItemDto.
     *
     * @param orderItem The order item entity to convert
     * @return The equivalent OrderItemDto
     */
    private OrderItemDto mapOrderItemToDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(orderItem.getProductId());
        dto.setProductName(orderItem.getProductName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        // Total price will be calculated in the OrderItemDto
        
        return dto;
    }
    
    /**
     * Maps an OrderItemDto to an OrderItem entity.
     *
     * @param itemDto The order item DTO to convert
     * @return The equivalent OrderItem entity
     */
    private OrderItem mapDtoToOrderItem(OrderItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(itemDto.getProductId());
        orderItem.setProductName(itemDto.getProductName());
        orderItem.setQuantity(itemDto.getQuantity());
        orderItem.setUnitPrice(itemDto.getUnitPrice());
        
        // FIXME: Ensure proper validation of product existence and availability
        
        return orderItem;
    }
    
    /**
     * Generates a unique order ID.
     * 
     * @return A unique order identifier
     */
    private String generateOrderId() {
        // Generate a random UUID and take first part as order ID
        // In a real application, this might follow specific business rules or sequences
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}