package com.gradlemedium200.client;

import com.gradlemedium200.config.RestTemplateConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for communicating with the ProductCatalog module.
 * Provides methods to interact with product catalog services including retrieving
 * product information, searching products, and checking product availability.
 */
@Component
public class ProductCatalogClient {
    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${product.service.url:http://localhost:8080/api/products}")
    private String productServiceUrl;
    
    @Value("${product.service.timeout:5000}")
    private int timeout;
    
    /**
     * Constructor that accepts a RestTemplate for HTTP communication
     * 
     * @param restTemplate REST template for HTTP communication
     */
    @Autowired
    public ProductCatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Initialize the client with proper configuration
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing ProductCatalogClient with service URL: {}", productServiceUrl);
        // Additional initialization logic can be implemented here if needed
    }
    
    /**
     * Retrieves product information by ID
     * 
     * @param productId unique identifier of the product
     * @return product information as an Object, or null if not found
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getProduct(String productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            logger.info("Getting product with ID: {}", productId);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found with ID: {}", productId, e);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving product with ID: {}", productId, e);
            throw new ResourceAccessException("Error retrieving product information: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves products by category with pagination
     * 
     * @param category product category
     * @param page page number (zero-based)
     * @return paginated list of products as an Object
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getProductsByCategory(String category, int page) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(productServiceUrl)
                .queryParam("category", category)
                .queryParam("page", page)
                .queryParam("size", 20) // Default page size
                .build()
                .toUriString();
                
            logger.info("Getting products for category: {}, page: {}", category, page);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error retrieving products for category: {}", category, e);
            throw new ResourceAccessException("Error retrieving products by category: " + e.getMessage());
        }
    }
    
    /**
     * Searches products with filters
     * 
     * @param searchTerm search keyword or phrase
     * @param filters additional search filters
     * @return search results as an Object
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object searchProducts(String searchTerm, Map<String, Object> filters) {
        try {
            // Create a base URI builder with the search term
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(productServiceUrl + "/search")
                .queryParam("term", searchTerm);
            
            // Add filters to the query parameters if provided
            if (filters != null) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    builder.queryParam(entry.getKey(), entry.getValue().toString());
                }
            }
            
            String url = builder.build().toUriString();
            logger.info("Searching products with term: {} and filters: {}", searchTerm, filters);
            
            // Execute search request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error searching products with term: {}", searchTerm, e);
            throw new ResourceAccessException("Error searching products: " + e.getMessage());
        }
    }
    
    /**
     * Checks if product is available in required quantity
     * 
     * @param productId unique identifier of the product
     * @param quantity requested quantity
     * @return true if the product is available in the requested quantity, false otherwise
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public boolean checkProductAvailability(String productId, int quantity) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(productServiceUrl + "/availability")
                .queryParam("productId", productId)
                .queryParam("quantity", quantity)
                .build()
                .toUriString();
                
            logger.info("Checking availability for product ID: {}, quantity: {}", productId, quantity);
            
            // Create request body for availability check
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", productId);
            requestBody.put("quantity", quantity);
            
            // Execute availability check
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            // Extract availability status from response
            if (response.getBody() != null && response.getBody().containsKey("available")) {
                return Boolean.TRUE.equals(response.getBody().get("available"));
            }
            
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found during availability check for ID: {}", productId);
            return false;
        } catch (Exception e) {
            logger.error("Error checking product availability for ID: {}", productId, e);
            throw new ResourceAccessException("Error checking product availability: " + e.getMessage());
        }
    }
    
    /**
     * Initializes user preferences with default values
     *
     * @param userId the user ID
     * @param preferences the default preferences
     * @return the initialized preferences
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object initializeUserPreferences(String userId, Map<String, Object> preferences) {
        try {
            String url = productServiceUrl.replace("/products", "/users/" + userId + "/preferences");
            logger.info("Initializing preferences for user: {}", userId);
            
            return restTemplate.postForObject(url, preferences, Object.class);
        } catch (Exception e) {
            logger.error("Error initializing user preferences for user ID: {}", userId, e);
            throw new ResourceAccessException("Error initializing user preferences: " + e.getMessage());
        }
    }
    
    /**
     * Validates availability of multiple products
     *
     * @param products list of products to validate
     * @return validation result
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object validateProductsAvailability(Object products) {
        try {
            String url = productServiceUrl + "/validate-availability";
            logger.info("Validating products availability");
            
            return restTemplate.postForObject(url, products, Object.class);
        } catch (Exception e) {
            logger.error("Error validating products availability", e);
            throw new ResourceAccessException("Error validating products availability: " + e.getMessage());
        }
    }
    
    /**
     * Gets user preferences from the product catalog service
     *
     * @param userId the user ID
     * @return user preferences
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getUserPreferences(String userId) {
        try {
            String url = productServiceUrl.replace("/products", "/users/" + userId + "/preferences");
            logger.info("Getting preferences for user: {}", userId);
            
            return restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Preferences not found for user: {}", userId);
            return null;
        } catch (Exception e) {
            logger.error("Error getting user preferences for user ID: {}", userId, e);
            throw new ResourceAccessException("Error getting user preferences: " + e.getMessage());
        }
    }
    
    /**
     * Gets recommended products for a user based on preferences
     *
     * @param userId the user ID
     * @param userPreferences the user preferences
     * @param filters additional filters
     * @return recommended products
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getRecommendedProducts(String userId, Object userPreferences, Map<String, Object> filters) {
        try {
            String url = productServiceUrl + "/recommendations/" + userId;
            logger.info("Getting recommended products for user: {}", userId);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("preferences", userPreferences);
            requestBody.put("filters", filters);
            
            return restTemplate.postForObject(url, requestBody, Object.class);
        } catch (Exception e) {
            logger.error("Error getting recommended products for user ID: {}", userId, e);
            throw new ResourceAccessException("Error getting recommended products: " + e.getMessage());
        }
    }
    
    /**
     * Gets batch of product details by IDs
     *
     * @param productIds the product IDs
     * @return map of product details
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getProductDetailsBatch(Object productIds) {
        try {
            String url = productServiceUrl + "/batch";
            logger.info("Getting product details batch");
            
            return restTemplate.postForObject(url, productIds, Object.class);
        } catch (Exception e) {
            logger.error("Error getting product details batch", e);
            throw new ResourceAccessException("Error getting product details batch: " + e.getMessage());
        }
    }
    
    /**
     * Checks availability of multiple products
     *
     * @param products the products to check
     * @return true if all products are available, false otherwise
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public boolean checkProductsAvailability(Object products) {
        try {
            String url = productServiceUrl + "/check-availability";
            logger.info("Checking products availability");
            
            Boolean result = restTemplate.postForObject(url, products, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error checking products availability", e);
            return false;
        }
    }
    
    /**
     * Validates preference categories
     *
     * @param preferences the preferences to validate
     * @return true if preferences are valid, false otherwise
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public boolean validatePreferenceCategories(Object preferences) {
        try {
            String url = productServiceUrl.replace("/products", "/preferences/validate");
            logger.info("Validating preference categories");
            
            Boolean result = restTemplate.postForObject(url, preferences, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error validating preference categories", e);
            return false;
        }
    }
    
    /**
     * Creates an HTTP entity with appropriate headers for API requests
     * 
     * @return HttpEntity with configured headers
     */
    private HttpEntity<Object> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        // TODO: Add authentication headers when security is implemented
        
        return new HttpEntity<>(headers);
    }
    
    /**
     * Creates an HTTP entity with a body and appropriate headers for API requests
     * 
     * @param body the request body
     * @return HttpEntity with configured headers and body
     */
    private HttpEntity<Object> createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        // TODO: Add authentication headers when security is implemented
        
        return new HttpEntity<>(body, headers);
    }
    
    /**
     * Sets the base URL for the product catalog service
     * 
     * @param productServiceUrl base URL for the product catalog service
     */
    public void setProductServiceUrl(String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
        logger.info("Product service URL updated to: {}", productServiceUrl);
    }
    
    /**
     * Sets the request timeout in milliseconds
     * 
     * @param timeout request timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        logger.info("Timeout updated to: {}ms", timeout);
        // FIXME: This doesn't actually update the RestTemplate timeout; need to implement proper timeout handling
    }
}