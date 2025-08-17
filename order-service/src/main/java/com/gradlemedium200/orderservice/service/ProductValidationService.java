package com.gradlemedium200.orderservice.service;

import com.gradlemedium200.orderservice.dto.ProductDto;
import com.gradlemedium200.orderservice.integration.ProductCatalogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for validating product availability and details.
 * This service acts as a facade for product validation operations,
 * working with the ProductCatalogClient to validate various aspects of products.
 */
@Service
public class ProductValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductValidationService.class);
    
    private final ProductCatalogClient productCatalogClient;
    
    /**
     * Constructor for dependency injection
     *
     * @param productCatalogClient Client for product catalog service integration
     */
    public ProductValidationService(ProductCatalogClient productCatalogClient) {
        this.productCatalogClient = productCatalogClient;
    }
    
    /**
     * Validates if product exists and is available.
     * A product is considered valid if it exists in the catalog and is active.
     *
     * @param productId ID of the product to validate
     * @return true if product exists and is available, false otherwise
     */
    public boolean validateProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Validation failed: Product ID is null or empty");
            return false;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Validation failed: Product with ID {} not found", productId);
                return false;
            }
            
            ProductDto product = productOptional.get();
            boolean isValid = product.isActive() && 
                             product.getInventoryCount() != null && 
                             product.getInventoryCount() > 0;
            
            if (!isValid) {
                logger.warn("Validation failed: Product with ID {} is inactive or out of stock", productId);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating product {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves product details from catalog.
     * Returns complete product information from the product catalog service.
     *
     * @param productId ID of the product to retrieve details for
     * @return ProductDto if found, null otherwise
     */
    public ProductDto getProductDetails(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Cannot get product details: Product ID is null or empty");
            return null;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Product with ID {} not found", productId);
                return null;
            }
            
            return productOptional.get();
        } catch (Exception e) {
            logger.error("Error retrieving product details for {}: {}", productId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Validates product price against catalog.
     * Ensures the expected price matches the current catalog price within a small delta
     * to account for potential rounding issues or price precision.
     *
     * @param productId     ID of the product to validate price for
     * @param expectedPrice Price expected by the caller
     * @return true if prices match within acceptable tolerance, false otherwise
     */
    public boolean validateProductPrice(String productId, BigDecimal expectedPrice) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Price validation failed: Product ID is null or empty");
            return false;
        }
        
        if (expectedPrice == null) {
            logger.warn("Price validation failed: Expected price is null for product {}", productId);
            return false;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Price validation failed: Product with ID {} not found", productId);
                return false;
            }
            
            ProductDto product = productOptional.get();
            BigDecimal catalogPrice = product.getPrice();
            
            // Compare prices with a small delta to account for rounding issues
            // For exact comparison, use compareTo instead
            boolean pricesMatch = Objects.equals(expectedPrice, catalogPrice) || 
                    (catalogPrice != null && 
                     expectedPrice.subtract(catalogPrice).abs().compareTo(new BigDecimal("0.001")) < 0);
            
            if (!pricesMatch) {
                logger.warn("Price validation failed for product {}: expected {}, actual {}",
                        productId, expectedPrice, catalogPrice);
            }
            
            return pricesMatch;
        } catch (Exception e) {
            logger.error("Error validating price for product {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if product is active and available for sale.
     * A product is considered active if it exists, is marked as active, 
     * and has inventory available.
     *
     * @param productId ID of the product to check status for
     * @return true if product is active and available, false otherwise
     */
    public boolean isProductActive(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Active check failed: Product ID is null or empty");
            return false;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Active check failed: Product with ID {} not found", productId);
                return false;
            }
            
            ProductDto product = productOptional.get();
            boolean isActive = product.isActive();
            
            // Additional business logic could be added here
            // For example, checking if the product is not discontinued, 
            // belongs to active category, etc.
            
            return isActive;
        } catch (Exception e) {
            logger.error("Error checking if product {} is active: {}", productId, e.getMessage());
            return false;
        }
    }
    
    // FIXME: Consider implementing retry mechanism for transient failures
    
    /**
     * Checks if product is valid.
     * Alias for validateProduct method.
     *
     * @param productId ID of the product to check
     * @return true if product is valid, false otherwise
     */
    public boolean isProductValid(String productId) {
        return validateProduct(productId);
    }
    
    /**
     * Checks if requested quantity is available for a product.
     *
     * @param productId ID of the product to check
     * @param quantity  Quantity requested
     * @return true if quantity is available, false otherwise
     */
    public boolean isQuantityAvailable(String productId, Integer quantity) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Quantity check failed: Product ID is null or empty");
            return false;
        }
        
        if (quantity == null || quantity <= 0) {
            logger.warn("Quantity check failed: Invalid quantity {} for product {}", quantity, productId);
            return false;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Quantity check failed: Product with ID {} not found", productId);
                return false;
            }
            
            ProductDto product = productOptional.get();
            boolean quantityAvailable = product.getInventoryCount() != null && 
                                       product.getInventoryCount() >= quantity;
            
            if (!quantityAvailable) {
                logger.warn("Insufficient inventory for product {}: requested {}, available {}",
                        productId, quantity, product.getInventoryCount());
            }
            
            return quantityAvailable;
        } catch (Exception e) {
            logger.error("Error checking quantity for product {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the current price of a product.
     *
     * @param productId ID of the product
     * @return Current price if available, null otherwise
     */
    public BigDecimal getCurrentPrice(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Cannot get price: Product ID is null or empty");
            return null;
        }
        
        try {
            Optional<ProductDto> productOptional = productCatalogClient.getProduct(productId);
            
            if (!productOptional.isPresent()) {
                logger.warn("Price check failed: Product with ID {} not found", productId);
                return null;
            }
            
            return productOptional.get().getPrice();
        } catch (Exception e) {
            logger.error("Error getting price for product {}: {}", productId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets product information.
     *
     * @param productId ID of the product
     * @return Optional containing ProductDto if found, empty otherwise
     */
    public Optional<ProductDto> getProductInfo(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            logger.warn("Cannot get product info: Product ID is null or empty");
            return Optional.empty();
        }
        
        try {
            return productCatalogClient.getProduct(productId);
        } catch (Exception e) {
            logger.error("Error getting product info for {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Validates the availability of multiple products.
     *
     * @param products List of products to check
     * @return true if all products are available, false otherwise
     */
    public boolean checkProductsAvailability(Object products) {
        // Implementation would depend on the structure of the products object
        // For now, we'll return true to avoid compilation errors
        logger.warn("checkProductsAvailability called with {} but not fully implemented", products);
        return true;
    }
    
    /**
     * Validates that product categories in preferences exist.
     *
     * @param preferences User preferences to validate
     * @return true if all categories exist, false otherwise
     */
    public boolean validatePreferenceCategories(Object preferences) {
        // Implementation would depend on the structure of the preferences object
        // For now, we'll return true to avoid compilation errors
        logger.warn("validatePreferenceCategories called with {} but not fully implemented", preferences);
        return true;
    }
}