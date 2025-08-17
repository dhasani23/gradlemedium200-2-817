package com.gradlemedium200.orderservice.service;

import com.gradlemedium200.orderservice.model.Order;
import com.gradlemedium200.orderservice.model.OrderItem;
import com.gradlemedium200.orderservice.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing orders.
 */
@Service
public class OrderService {

    private final Map<String, Order> orderRepository = new HashMap<>();

    /**
     * Creates a new order.
     *
     * @param order The order to create
     * @return The created order
     */
    public Order createOrder(Order order) {
        String orderId = UUID.randomUUID().toString();
        order.setOrderId(orderId);
        order.setId(orderId);
        
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING.name());
        }
        
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(OrderStatus.PENDING);
        }
        
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(LocalDateTime.now());
        }
        
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
            order.setOrderItems(new ArrayList<>());
        }
        
        // Calculate total amount if not set
        if (order.getTotalAmount() == null) {
            order.calculateTotalAmount();
        }
        
        orderRepository.put(orderId, order);
        return order;
    }

    /**
     * Gets an order by ID.
     *
     * @param orderId The order ID
     * @return The order, or null if not found
     */
    public Order getOrder(String orderId) {
        return orderRepository.get(orderId);
    }

    /**
     * Updates an existing order.
     *
     * @param orderId The order ID
     * @param order The updated order data
     * @return The updated order, or null if not found
     */
    public Order updateOrder(String orderId, Order order) {
        if (!orderRepository.containsKey(orderId)) {
            return null;
        }
        
        order.setOrderId(orderId);
        order.setId(orderId);
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.put(orderId, order);
        return order;
    }

    /**
     * Deletes an order.
     *
     * @param orderId The order ID
     * @return True if the order was deleted, false if not found
     */
    public boolean deleteOrder(String orderId) {
        if (!orderRepository.containsKey(orderId)) {
            return false;
        }
        
        orderRepository.remove(orderId);
        return true;
    }

    /**
     * Gets all orders.
     *
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orderRepository.values());
    }

    /**
     * Gets all orders for a customer.
     *
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    public List<Order> getOrdersByCustomer(String customerId) {
        List<Order> customerOrders = new ArrayList<>();
        
        for (Order order : orderRepository.values()) {
            if (customerId.equals(order.getCustomerId()) || customerId.equals(order.getUserId())) {
                customerOrders.add(order);
            }
        }
        
        return customerOrders;
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId The order ID
     * @param status The new status as OrderStatus
     * @return The updated order, or null if not found
     */
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.get(orderId);
        
        if (order == null) {
            return null;
        }
        
        order.setStatus(status.name());
        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.put(orderId, order);
        return order;
    }
    
    /**
     * Updates the status of an order using string status.
     *
     * @param orderId The order ID
     * @param statusStr The new status as a string
     * @return The updated order, or null if not found
     */
    public Order updateOrderStatus(String orderId, String statusStr) {
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to PENDING if the status string is invalid
            status = OrderStatus.PENDING;
        }
        
        return updateOrderStatus(orderId, status);
    }

    /**
     * Adds an item to an order.
     *
     * @param orderId The order ID
     * @param item The item to add
     * @return The updated order, or null if not found
     */
    public Order addOrderItem(String orderId, OrderItem item) {
        Order order = orderRepository.get(orderId);
        
        if (order == null) {
            return null;
        }
        
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        }
        
        order.addOrderItem(item);
        
        orderRepository.put(orderId, order);
        return order;
    }
    
    /**
     * Process the order after creation
     * 
     * @param orderId The ID of the order to process
     * @return The processed order
     */
    public Order processOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            return null;
        }
        
        // Simply update the status to PROCESSING
        return updateOrderStatus(orderId, OrderStatus.PROCESSING);
    }
}