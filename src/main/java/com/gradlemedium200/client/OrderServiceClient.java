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
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with the OrderService module.
 * Provides methods to interact with order services including creating orders,
 * retrieving order information, and updating order status.
 */
@Component
public class OrderServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceClient.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${order.service.url:http://localhost:8080/api/orders}")
    private String orderServiceUrl;
    
    @Value("${order.service.timeout:10000}")
    private int timeout;
    
    /**
     * Constructor that accepts a RestTemplate for HTTP communication
     * 
     * @param restTemplate REST template for HTTP communication
     */
    @Autowired
    public OrderServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Initialize the client with proper configuration
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing OrderServiceClient with service URL: {}", orderServiceUrl);
        // Additional initialization logic can be implemented here if needed
    }
    
    /**
     * Creates a new order
     * 
     * @param orderData the order data containing customer details, items, and shipping information
     * @return created order information as an Object, or null if creation failed
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object createOrder(Object orderData) {
        try {
            logger.info("Creating new order");
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                orderServiceUrl,
                HttpMethod.POST,
                createHttpEntity(orderData),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            logger.info("Order created successfully");
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error creating order", e);
            throw new ResourceAccessException("Error creating order: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves order information by ID
     * 
     * @param orderId unique identifier of the order
     * @return order information as an Object, or null if not found
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getOrder(String orderId) {
        try {
            String url = orderServiceUrl + "/" + orderId;
            logger.info("Getting order with ID: {}", orderId);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Order not found with ID: {}", orderId, e);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving order with ID: {}", orderId, e);
            throw new ResourceAccessException("Error retrieving order information: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves orders for a specific user with pagination
     * 
     * @param userId unique identifier of the user
     * @param page page number (zero-based)
     * @return paginated list of orders as an Object
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object getOrdersByUser(String userId, int page) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .queryParam("userId", userId)
                .queryParam("page", page)
                .queryParam("size", 10) // Default page size
                .build()
                .toUriString();
                
            logger.info("Getting orders for user: {}, page: {}", userId, page);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error retrieving orders for user: {}", userId, e);
            throw new ResourceAccessException("Error retrieving orders by user: " + e.getMessage());
        }
    }
    
    /**
     * Updates the status of an order
     * 
     * @param orderId unique identifier of the order
     * @param status new status to set for the order
     * @return updated order information as an Object
     * @throws ResourceAccessException if there is an issue connecting to the service
     */
    public Object updateOrderStatus(String orderId, String status) {
        try {
            String url = orderServiceUrl + "/" + orderId + "/status";
            logger.info("Updating order status: {} to {}", orderId, status);
            
            // Create request body with status update
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("status", status);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                createHttpEntity(requestBody),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            logger.info("Order status updated successfully");
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Order not found with ID: {}", orderId, e);
            return null;
        } catch (Exception e) {
            logger.error("Error updating order status for ID: {}", orderId, e);
            throw new ResourceAccessException("Error updating order status: " + e.getMessage());
        }
    }
    
    /**
     * Get user's orders with advanced filtering
     *
     * @param userId the user ID
     * @param filters additional filters
     * @return user orders with applied filters
     */
    public Object getUserOrders(String userId, Map<String, Object> filters) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(orderServiceUrl + "/user/" + userId);
            
            // Add filters to the query parameters if provided
            if (filters != null) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    builder.queryParam(entry.getKey(), entry.getValue().toString());
                }
            }
            
            String url = builder.build().toUriString();
            logger.info("Getting orders for user: {} with filters", userId);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error retrieving orders for user: {} with filters", userId, e);
            throw new ResourceAccessException("Error retrieving user orders: " + e.getMessage());
        }
    }
    
    /**
     * Get order details
     *
     * @param orderId the order ID
     * @return detailed order information
     */
    public Object getOrderDetails(String orderId) {
        try {
            String url = orderServiceUrl + "/" + orderId + "/details";
            logger.info("Getting detailed order information for ID: {}", orderId);
            
            return restTemplate.getForObject(url, Object.class);
        } catch (Exception e) {
            logger.error("Error retrieving order details for ID: {}", orderId, e);
            return null;
        }
    }
    
    /**
     * Get user order history
     *
     * @param userId the user ID
     * @param limit maximum number of orders to return
     * @return user's order history
     */
    public Object getUserOrderHistory(String userId, Integer limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl + "/history/" + userId)
                .queryParam("limit", limit)
                .build()
                .toUriString();
            
            logger.info("Getting order history for user: {}, limit: {}", userId, limit);
            
            return restTemplate.getForObject(url, Object.class);
        } catch (Exception e) {
            logger.error("Error retrieving order history for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Check if a user is within their order limits
     *
     * @param userId the user ID
     * @param orderData the order data to check
     * @return true if the user is within their order limits, false otherwise
     */
    public boolean checkUserOrderLimits(String userId, Object orderData) {
        try {
            String url = orderServiceUrl + "/limits/check";
            logger.info("Checking order limits for user: {}", userId);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("orderData", orderData);
            
            Boolean result = restTemplate.postForObject(url, requestBody, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error checking order limits for user: {}", userId, e);
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
     * Sets the base URL for the order service
     * 
     * @param orderServiceUrl base URL for the order service
     */
    public void setOrderServiceUrl(String orderServiceUrl) {
        this.orderServiceUrl = orderServiceUrl;
        logger.info("Order service URL updated to: {}", orderServiceUrl);
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