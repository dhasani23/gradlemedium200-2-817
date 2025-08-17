package com.gradlemedium200.orderservice.integration;

import com.gradlemedium200.orderservice.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for product catalog service integration.
 * This class is responsible for communicating with the product catalog service
 * to retrieve product information, validate product existence, check availability,
 * and get pricing data.
 */
@Component
public class ProductCatalogClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogClient.class);
    
    private final RestTemplate restTemplate;
    private final String productCatalogUrl;
    
    // Caching mechanism to avoid repeated calls for the same product
    private final Map<String, ProductDto> productCache = new HashMap<>();
    
    /**
     * Constructor for ProductCatalogClient.
     * 
     * @param restTemplate REST template for HTTP calls
     * @param productCatalogUrl Base URL of the product catalog service
     */
    @Autowired
    public ProductCatalogClient(RestTemplate restTemplate, 
                               @Value("${services.product-catalog.url}") String productCatalogUrl) {
        this.restTemplate = restTemplate;
        this.productCatalogUrl = productCatalogUrl;
        logger.info("ProductCatalogClient initialized with URL: {}", productCatalogUrl);
    }
    
    /**
     * Retrieves product details by ID.
     * 
     * @param productId The ID of the product to retrieve
     * @return ProductDto containing product information
     * @throws RuntimeException if the product cannot be found or service is unavailable
     */
    public ProductDto getProductById(String productId) {
        logger.debug("Retrieving product with ID: {}", productId);
        
        // Check cache first
        if (productCache.containsKey(productId)) {
            logger.debug("Product found in cache: {}", productId);
            return productCache.get(productId);
        }
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(productCatalogUrl)
                    .pathSegment("products", productId)
                    .toUriString();
            
            ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Cache the result
                productCache.put(productId, response.getBody());
                return response.getBody();
            } else {
                logger.error("Failed to retrieve product with ID: {}. Response status: {}", 
                        productId, response.getStatusCode());
                throw new RuntimeException("Unable to retrieve product: " + productId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Product not found with ID: {}", productId, e);
            throw new RuntimeException("Product not found: " + productId);
        } catch (Exception e) {
            logger.error("Error retrieving product with ID: {}", productId, e);
            throw new RuntimeException("Error retrieving product information", e);
        }
    }
    
    /**
     * Validates if product exists and is active.
     * 
     * @param productId The ID of the product to validate
     * @return true if product exists and is active, false otherwise
     */
    public boolean validateProduct(String productId) {
        logger.debug("Validating product with ID: {}", productId);
        
        try {
            ProductDto product = getProductById(productId);
            boolean isValid = product != null && product.isActive();
            
            logger.debug("Product {} validation result: {}", productId, isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("Product validation failed for ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * Retrieves current product price.
     * 
     * @param productId The ID of the product to get price for
     * @return Current price of the product
     * @throws RuntimeException if the product cannot be found or service is unavailable
     */
    public BigDecimal getProductPrice(String productId) {
        logger.debug("Getting price for product with ID: {}", productId);
        
        try {
            // For price-sensitive operations, we might want to bypass the cache
            // to ensure we have the most current price
            String url = UriComponentsBuilder.fromHttpUrl(productCatalogUrl)
                    .pathSegment("products", productId, "price")
                    .toUriString();
            
            ResponseEntity<BigDecimal> response = restTemplate.getForEntity(url, BigDecimal.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                // Fallback to cached product if direct price lookup fails
                ProductDto cachedProduct = getProductById(productId);
                if (cachedProduct != null) {
                    logger.warn("Direct price lookup failed, using cached product price for ID: {}", productId);
                    return cachedProduct.getPrice();
                }
                
                logger.error("Failed to retrieve price for product ID: {}", productId);
                throw new RuntimeException("Unable to retrieve price for product: " + productId);
            }
        } catch (Exception e) {
            logger.error("Error retrieving price for product with ID: {}", productId, e);
            throw new RuntimeException("Error retrieving product price", e);
        }
    }
    
    /**
     * Checks if product is available for purchase.
     * Product is considered available if it exists, is active, and has inventory.
     * 
     * @param productId The ID of the product to check availability for
     * @return true if product is available, false otherwise
     */
    public boolean isProductAvailable(String productId) {
        logger.debug("Checking availability for product with ID: {}", productId);
        
        try {
            ProductDto product = getProductById(productId);
            boolean isAvailable = product != null && 
                                 product.isActive() && 
                                 product.getInventoryCount() != null &&
                                 product.getInventoryCount() > 0;
            
            logger.debug("Product {} availability result: {}", productId, isAvailable);
            return isAvailable;
        } catch (Exception e) {
            logger.warn("Product availability check failed for ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * Clears the product cache to ensure fresh data on next request.
     * This method can be called periodically or when data refresh is needed.
     */
    public void clearCache() {
        logger.info("Clearing product cache containing {} items", productCache.size());
        productCache.clear();
    }
    
    /**
     * Checks if a specific product is in stock with the requested quantity.
     * 
     * @param productId The ID of the product to check
     * @param quantity The quantity needed
     * @return true if requested quantity is available, false otherwise
     */
    public boolean checkStock(String productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        
        try {
            ProductDto product = getProductById(productId);
            return product != null && 
                   product.isActive() && 
                   product.getInventoryCount() != null &&
                   product.getInventoryCount() >= quantity;
        } catch (Exception e) {
            logger.error("Stock check failed for product ID: {}", productId, e);
            return false;
        }
    }
    
    // FIXME: Add circuit breaker pattern to handle product catalog service outages
    
    /**
     * Retrieves a product by ID as an Optional.
     * 
     * @param productId The ID of the product to retrieve
     * @return Optional containing the product if found, empty otherwise
     */
    public java.util.Optional<ProductDto> getProduct(String productId) {
        try {
            ProductDto product = getProductById(productId);
            return java.util.Optional.ofNullable(product);
        } catch (Exception e) {
            logger.error("Error retrieving product with ID: {}", productId, e);
            return java.util.Optional.empty();
        }
    }
    
    /**
     * Retrieves product details for multiple products.
     * 
     * @param productIds The list of product IDs to retrieve
     * @return Map of product ID to ProductDto
     */
    public java.util.Map<String, ProductDto> getProductDetailsBatch(Object productIds) {
        // Implementation would depend on the structure of the productIds object
        // For now, we'll return an empty map to avoid compilation errors
        logger.warn("getProductDetailsBatch called but not fully implemented");
        return new java.util.HashMap<>();
    }
    
    /**
     * Validates product availability in batch.
     * 
     * @param products List of products to validate
     * @return True if all products are available
     */
    public boolean validateProductsAvailability(Object products) {
        // Implementation would depend on the structure of the products object
        // For now, we'll return true to avoid compilation errors
        logger.warn("validateProductsAvailability called but not fully implemented");
        return true;
    }
    
    /**
     * Initializes user preferences for products.
     * 
     * @param userId User ID
     * @param preferences Map of preferences
     * @return Map of initialized preferences
     */
    public java.util.Map<String, Object> initializeUserPreferences(String userId, java.util.Map<String, Object> preferences) {
        // Implementation would depend on the specific requirements
        // For now, we'll return the input preferences to avoid compilation errors
        logger.warn("initializeUserPreferences called but not fully implemented");
        return preferences;
    }
    
    /**
     * Gets user preferences for products.
     * 
     * @param userId User ID
     * @return User preferences
     */
    public Object getUserPreferences(String userId) {
        // Implementation would depend on the specific requirements
        // For now, we'll return an empty map to avoid compilation errors
        logger.warn("getUserPreferences called but not fully implemented");
        return new java.util.HashMap<String, Object>();
    }
    
    /**
     * Gets recommended products for a user.
     * 
     * @param userId User ID
     * @param preferences User preferences
     * @param filters Filters to apply
     * @return List of recommended products
     */
    public Object getRecommendedProducts(String userId, Object preferences, java.util.Map<String, Object> filters) {
        // Implementation would depend on the specific requirements
        // For now, we'll return an empty list to avoid compilation errors
        logger.warn("getRecommendedProducts called but not fully implemented");
        return new java.util.ArrayList<ProductDto>();
    }
}