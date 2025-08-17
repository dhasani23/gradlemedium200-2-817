package com.gradlemedium200.productcatalog.controller;

import com.gradlemedium200.productcatalog.dto.InventoryDto;
import com.gradlemedium200.productcatalog.exception.InsufficientInventoryException;
import com.gradlemedium200.productcatalog.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * REST controller for inventory tracking and management operations.
 * Provides API endpoints for checking product availability, getting inventory status,
 * finding low stock items, and updating inventory levels.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    private final InventoryService inventoryService;

    /**
     * Constructor with dependency injection
     *
     * @param inventoryService Service for inventory operations
     */
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get inventory status for a specific product
     *
     * @param productId The unique identifier of the product
     * @return Inventory information as DTO with HTTP status
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDto> getInventoryStatus(@PathVariable String productId) {
        logger.info("Received request to get inventory status for product: {}", productId);
        
        InventoryDto inventoryDto = inventoryService.getInventoryStatus(productId);
        
        if (inventoryDto == null) {
            logger.warn("No inventory found for product: {}", productId);
            return ResponseEntity.notFound().build();
        }
        
        logger.debug("Retrieved inventory status for product: {}", productId);
        return ResponseEntity.ok(inventoryDto);
    }

    /**
     * Check if a specific quantity of a product is available
     *
     * @param productId The unique identifier of the product
     * @param quantity The quantity to check for availability
     * @return Boolean indicating availability with HTTP status
     */
    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable String productId,
            @RequestParam @Min(1) int quantity) {
        
        logger.info("Checking availability of product: {} for quantity: {}", productId, quantity);
        
        if (quantity <= 0) {
            logger.warn("Invalid quantity parameter: {}. Must be greater than 0.", quantity);
            return ResponseEntity.badRequest().body(false);
        }
        
        boolean isAvailable = inventoryService.checkAvailability(productId, quantity);
        
        logger.debug("Product {} availability for quantity {}: {}", productId, quantity, isAvailable);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Get list of products with low stock levels
     *
     * @return List of inventory items that are below reorder level
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStockItems() {
        logger.info("Received request to get low stock items");
        
        List<InventoryDto> lowStockItems = inventoryService.getLowStockItems();
        
        if (lowStockItems.isEmpty()) {
            logger.info("No low stock items found");
            // Return empty list with 200 OK status instead of 204 No Content
            // to make it easier for clients to process the response
            return ResponseEntity.ok(lowStockItems);
        }
        
        logger.debug("Found {} low stock items", lowStockItems.size());
        return ResponseEntity.ok(lowStockItems);
    }

    /**
     * Update inventory quantity for a product
     *
     * @param productId The unique identifier of the product
     * @param quantityChange The change in quantity (positive for increase, negative for decrease)
     * @return Updated inventory information as DTO with HTTP status
     */
    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable String productId,
            @RequestParam int quantityChange) {
        
        logger.info("Updating inventory for product: {} by {} units", productId, quantityChange);
        
        try {
            InventoryDto updatedInventory = inventoryService.updateInventory(productId, quantityChange);
            
            if (updatedInventory == null) {
                logger.warn("Product not found: {}", productId);
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("Successfully updated inventory for product: {}", productId);
            return ResponseEntity.ok(updatedInventory);
            
        } catch (InsufficientInventoryException e) {
            // Cannot reduce inventory below zero
            logger.error("Insufficient inventory for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error updating inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Reserve inventory for an order
     *
     * @param productId The unique identifier of the product
     * @param quantity The quantity to reserve
     * @return Success indicator with HTTP status
     * 
     * TODO: Consider moving this to a separate OrderController or integrating with order processing
     */
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Boolean> reserveInventory(
            @PathVariable String productId,
            @RequestParam @Min(1) int quantity) {
        
        logger.info("Attempting to reserve {} units of product {}", quantity, productId);
        
        try {
            boolean reserved = inventoryService.reserveInventory(productId, quantity);
            
            if (reserved) {
                logger.debug("Successfully reserved inventory for product: {}", productId);
                return ResponseEntity.ok(true);
            } else {
                logger.warn("Failed to reserve inventory for product: {}", productId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
            }
        } catch (InsufficientInventoryException e) {
            logger.error("Insufficient inventory to reserve: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        } catch (Exception e) {
            logger.error("Error reserving inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    /**
     * Release previously reserved inventory (e.g., when an order is cancelled)
     *
     * @param productId The unique identifier of the product
     * @param quantity The quantity to release from reservation
     * @return Success indicator with HTTP status
     * 
     * FIXME: Add proper security to prevent unauthorized inventory releases
     */
    @PostMapping("/{productId}/release")
    public ResponseEntity<Boolean> releaseReservedInventory(
            @PathVariable String productId,
            @RequestParam @Min(1) int quantity) {
        
        logger.info("Attempting to release {} units of reserved inventory for product {}", quantity, productId);
        
        boolean released = inventoryService.releaseReservedInventory(productId, quantity);
        
        if (released) {
            logger.debug("Successfully released reserved inventory for product: {}", productId);
            return ResponseEntity.ok(true);
        } else {
            logger.warn("Failed to release reserved inventory for product: {}", productId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }
}