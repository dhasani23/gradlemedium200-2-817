package com.gradlemedium200.productcatalog.service;

import com.gradlemedium200.productcatalog.dto.InventoryDto;
import com.gradlemedium200.productcatalog.exception.InsufficientInventoryException;
import com.gradlemedium200.productcatalog.model.Inventory;
import com.gradlemedium200.productcatalog.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for inventory tracking, stock management, and availability checking.
 * Provides business logic for inventory operations including availability checks,
 * inventory reservations, and stock level management.
 */
@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final InventoryRepository inventoryRepository;
    private final NotificationService notificationService;

    /**
     * Constructor with dependencies injected.
     *
     * @param inventoryRepository Repository for inventory data access
     * @param notificationService Service for sending inventory notifications
     */
    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, NotificationService notificationService) {
        this.inventoryRepository = inventoryRepository;
        this.notificationService = notificationService;
    }

    /**
     * Check if requested quantity of a product is available in inventory.
     *
     * @param productId The product identifier
     * @param quantity The quantity requested
     * @return True if requested quantity is available, false otherwise
     */
    public boolean checkAvailability(String productId, int quantity) {
        logger.debug("Checking availability of product {} for quantity {}", productId, quantity);
        
        if (quantity <= 0) {
            logger.warn("Invalid quantity request: {}. Quantity must be positive.", quantity);
            return false;
        }

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Product {} not found in inventory", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        int availableQuantity = inventory.getQuantityAvailable() - inventory.getQuantityReserved();
        
        boolean isAvailable = availableQuantity >= quantity;
        logger.debug("Product {} availability check result: {}. Available: {}, Requested: {}", 
                productId, isAvailable, availableQuantity, quantity);
        
        return isAvailable;
    }

    /**
     * Reserve inventory for an order. This reduces available inventory without actually
     * consuming it until the order is confirmed.
     *
     * @param productId The product identifier
     * @param quantity The quantity to reserve
     * @return True if reservation was successful, false otherwise
     * @throws InsufficientInventoryException if there's not enough inventory to fulfill the reservation
     */
    public boolean reserveInventory(String productId, int quantity) {
        logger.debug("Attempting to reserve {} units of product {}", quantity, productId);
        
        if (quantity <= 0) {
            logger.warn("Invalid quantity for reservation: {}. Quantity must be positive.", quantity);
            return false;
        }

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Cannot reserve inventory - product {} not found", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        int availableQuantity = inventory.getQuantityAvailable() - inventory.getQuantityReserved();
        
        if (availableQuantity < quantity) {
            logger.warn("Insufficient inventory for product {}. Requested: {}, Available: {}", 
                    productId, quantity, availableQuantity);
            throw new InsufficientInventoryException(productId, quantity, availableQuantity);
        }
        
        // Reserve the quantity
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        logger.info("Successfully reserved {} units of product {}", quantity, productId);
        
        // Check if we need to send low stock notification after reservation
        checkAndNotifyLowStock(inventory);
        
        return true;
    }

    /**
     * Update inventory levels for a product. Can increase or decrease available quantity.
     *
     * @param productId The product identifier
     * @param quantityChange The change in quantity (positive for increase, negative for decrease)
     * @return Updated inventory information as DTO
     * @throws InsufficientInventoryException if decrease would result in negative inventory
     */
    public InventoryDto updateInventory(String productId, int quantityChange) {
        logger.debug("Updating inventory for product {} by {} units", productId, quantityChange);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Cannot update inventory - product {} not found", productId);
            return null;
        }
        
        Inventory inventory = inventoryOpt.get();
        int newQuantity = inventory.getQuantityAvailable() + quantityChange;
        
        // Prevent negative inventory
        if (newQuantity < 0) {
            logger.warn("Cannot update inventory - would result in negative stock for product {}", productId);
            throw new InsufficientInventoryException(
                "Inventory update would result in negative stock", 
                productId, 
                Math.abs(quantityChange), 
                inventory.getQuantityAvailable()
            );
        }
        
        // Update the quantity
        inventory.setQuantityAvailable(newQuantity);
        
        // If we're adding inventory, update the last restocked timestamp
        if (quantityChange > 0) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }
        
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        logger.info("Successfully updated inventory for product {}. New quantity: {}", productId, newQuantity);
        
        // Check if we need to send low stock notification after update
        checkAndNotifyLowStock(inventory);
        
        return convertToDto(inventory);
    }

    /**
     * Get current inventory status for a product.
     *
     * @param productId The product identifier
     * @return Current inventory information as DTO
     */
    public InventoryDto getInventoryStatus(String productId) {
        logger.debug("Getting inventory status for product {}", productId);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Inventory not found for product {}", productId);
            return null;
        }
        
        Inventory inventory = inventoryOpt.get();
        return convertToDto(inventory);
    }

    /**
     * Get list of items with low stock (below or at reorder level).
     *
     * @return List of inventory items with low stock
     */
    public List<InventoryDto> getLowStockItems() {
        logger.debug("Retrieving low stock items");
        
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        
        List<InventoryDto> lowStockDtos = lowStockItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("Found {} items with low stock", lowStockDtos.size());
        return lowStockDtos;
    }
    
    /**
     * Release previously reserved inventory (e.g., when an order is cancelled).
     *
     * @param productId The product identifier
     * @param quantity The quantity to release from reservation
     * @return true if the release was successful, false otherwise
     */
    public boolean releaseReservedInventory(String productId, int quantity) {
        logger.debug("Releasing {} units of reserved inventory for product {}", quantity, productId);
        
        if (quantity <= 0) {
            logger.warn("Invalid quantity for release: {}. Quantity must be positive.", quantity);
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (!inventoryOpt.isPresent()) {
            logger.warn("Cannot release inventory - product {} not found", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        // Don't release more than what is actually reserved
        int actualRelease = Math.min(quantity, inventory.getQuantityReserved());
        
        if (actualRelease > 0) {
            inventory.setQuantityReserved(inventory.getQuantityReserved() - actualRelease);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
            
            logger.info("Released {} units of reserved inventory for product {}", actualRelease, productId);
            return true;
        }
        
        logger.warn("No reserved inventory to release for product {}", productId);
        return false;
    }
    
    /**
     * Checks if inventory is low and sends notification if needed.
     *
     * @param inventory The inventory to check
     */
    private void checkAndNotifyLowStock(Inventory inventory) {
        if (inventory.isLowStock()) {
            logger.warn("Low stock detected for product {}. Available: {}, Reorder level: {}", 
                    inventory.getProductId(), inventory.getQuantityAvailable(), inventory.getReorderLevel());
            
            try {
                notificationService.sendInventoryAlert(inventory.getProductId(), inventory.getQuantityAvailable());
                logger.info("Low stock notification sent for product {}", inventory.getProductId());
            } catch (Exception e) {
                // Don't let notification failure affect the inventory operation
                logger.error("Failed to send low stock notification: {}", e.getMessage(), e);
                // TODO: Implement retry mechanism for failed notifications
            }
        }
    }

    /**
     * Converts an Inventory entity to an InventoryDto for API responses.
     *
     * @param inventory The inventory entity
     * @return The inventory DTO
     */
    private InventoryDto convertToDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        
        InventoryDto dto = new InventoryDto();
        dto.setProductId(inventory.getProductId());
        dto.setWarehouseId(inventory.getWarehouseId());
        dto.setQuantityAvailable(inventory.getQuantityAvailable());
        dto.setQuantityReserved(inventory.getQuantityReserved());
        dto.setTotalQuantity(inventory.getQuantityAvailable());  // This should be total physical quantity
        dto.setReorderLevel(inventory.getReorderLevel());
        dto.setLowStock(inventory.isLowStock());
        dto.setLastUpdated(inventory.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Batch check availability for multiple products.
     * 
     * @param productQuantities Map of product IDs to required quantities
     * @return List of product IDs that are unavailable in requested quantities
     * 
     * TODO: Implement this method for bulk availability checks
     */
    public List<String> batchCheckAvailability(java.util.Map<String, Integer> productQuantities) {
        // This is a placeholder for future implementation
        // Should check multiple products at once for better performance
        return new ArrayList<>();
    }
    
    /**
     * Create new inventory record for a product.
     * 
     * @param inventoryDto The inventory data to create
     * @return The created inventory record
     * 
     * FIXME: Implement proper validation and error handling
     */
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        // This is a placeholder for future implementation
        logger.info("Creating new inventory record for product {}", inventoryDto.getProductId());
        
        // This would create a new inventory record in the database
        return null;
    }
}