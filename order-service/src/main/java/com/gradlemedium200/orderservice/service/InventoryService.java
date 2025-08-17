package com.gradlemedium200.orderservice.service;

import com.gradlemedium200.orderservice.dto.InventoryCheckDto;
import com.gradlemedium200.orderservice.integration.InventoryClient;
import com.gradlemedium200.orderservice.model.OrderItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for inventory validation and reservation during order processing.
 * This service integrates with the inventory system to check product availability,
 * reserve inventory for orders, release inventory for canceled orders, and update
 * inventory after order fulfillment.
 *
 * @author gradlemedium200
 * @version 1.0
 */
@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final InventoryClient inventoryClient;
    
    /**
     * Constructor for dependency injection
     * 
     * @param inventoryClient Client for inventory service integration
     */
    @Autowired
    public InventoryService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }
    
    /**
     * Checks if the requested product and quantity are available in inventory.
     * 
     * @param inventoryCheck DTO containing product and quantity information
     * @return boolean indicating if inventory is available
     */
    public boolean checkInventory(InventoryCheckDto inventoryCheck) {
        if (inventoryCheck == null || inventoryCheck.getProductId() == null) {
            logger.error("Invalid inventory check request - missing required data");
            return false;
        }
        
        try {
            logger.info("Checking inventory for product: {} with quantity: {}", 
                    inventoryCheck.getProductId(), inventoryCheck.getRequestedQuantity());
            
            boolean isAvailable = inventoryClient.checkInventory(inventoryCheck);
            
            if (!isAvailable) {
                logger.warn("Insufficient inventory for product: {}, requested: {}, available: {}", 
                    inventoryCheck.getProductId(),
                    inventoryCheck.getRequestedQuantity(),
                    inventoryCheck.getAvailableQuantity());
            }
            
            return isAvailable;
        } catch (Exception e) {
            logger.error("Error checking inventory availability: {}", e.getMessage(), e);
            // FIXME: Consider more sophisticated fallback strategy for inventory check failures
            return false;
        }
    }
    
    /**
     * Reserves inventory for all items in an order.
     * This is typically called during order processing to ensure inventory
     * is reserved before payment is processed.
     * 
     * @param orderId unique identifier for the order
     * @param orderItems list of items in the order
     * @return boolean indicating if all items were successfully reserved
     */
    public boolean reserveInventory(String orderId, List<OrderItem> orderItems) {
        if (orderId == null || orderItems == null || orderItems.isEmpty()) {
            logger.error("Cannot reserve inventory - invalid order data");
            return false;
        }
        
        logger.info("Reserving inventory for order: {}, {} items", orderId, orderItems.size());
        
        // Track success/failure for each product reservation
        Map<String, Boolean> reservationResults = new HashMap<>();
        
        try {
            // Process each order item
            for (OrderItem item : orderItems) {
                boolean reserved = inventoryClient.reserveInventory(
                    orderId, 
                    item.getProductId(),
                    item.getQuantity()
                );
                
                reservationResults.put(item.getProductId(), reserved);
                
                if (!reserved) {
                    logger.warn("Failed to reserve inventory for product: {} in order: {}", 
                            item.getProductId(), orderId);
                }
            }
            
            // Check if all items were successfully reserved
            boolean allReserved = reservationResults.values().stream()
                    .allMatch(Boolean::booleanValue);
                
            if (!allReserved) {
                // If any reservation failed, attempt to release all successful reservations
                // TODO: Implement compensation logic to release partial reservations
                logger.warn("Not all inventory could be reserved for order: {}, compensation needed", orderId);
            }
            
            return allReserved;
        } catch (Exception e) {
            logger.error("Error reserving inventory for order: {}, error: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Releases previously reserved inventory for cancelled orders.
     * This ensures that inventory items are made available again when orders are cancelled.
     * 
     * @param orderId unique identifier for the order
     */
    public void releaseInventory(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Cannot release inventory - invalid order ID");
            return;
        }
        
        logger.info("Releasing reserved inventory for order: {}", orderId);
        
        try {
            // Call inventory service to release all inventory associated with this order
            inventoryClient.releaseInventory(orderId);
            logger.info("Successfully released inventory for order: {}", orderId);
        } catch (Exception e) {
            logger.error("Failed to release inventory for order: {}, error: {}", orderId, e.getMessage(), e);
            // FIXME: Implement retry mechanism for failed inventory release
            // This is critical to avoid inventory becoming permanently reserved
        }
    }
    
    /**
     * Updates inventory after order fulfillment.
     * This is typically called when an order is shipped to finalize the inventory changes.
     * 
     * @param orderId unique identifier for the order
     */
    public void updateInventory(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.error("Cannot update inventory - invalid order ID");
            return;
        }
        
        logger.info("Updating inventory after fulfillment for order: {}", orderId);
        
        try {
            // For orders that have been fulfilled (shipped), we may need to update 
            // inventory records from "reserved" to "consumed" state
            
            // Consider executing this asynchronously since it's not critical for immediate user feedback
            CompletableFuture.runAsync(() -> {
                try {
                    // Implementation depends on how inventory tracking is structured
                    // This might involve fetching order details first to get the items
                    // TODO: Refactor to handle inventory updates in batch for better performance
                    
                    // We need to fetch the order items and update inventory for each product
                    // For now, we'll use a placeholder implementation
                    logger.info("Updating inventory for order: {}", orderId);
                    
                    // FIXME: This should be replaced with actual order retrieval logic
                    // For now, just log that we would be doing the update
                    logger.info("Inventory would be updated for order: {}", orderId);
                } catch (Exception e) {
                    logger.error("Async inventory update failed for order: {}", orderId, e);
                }
            });
        } catch (Exception e) {
            logger.error("Error initiating inventory update for order: {}, error: {}", orderId, e.getMessage(), e);
        }
    }
}