package com.gradlemedium200.controller;

import com.gradlemedium200.dto.ApiResponse;
import com.gradlemedium200.dto.HealthStatus;
import com.gradlemedium200.service.HealthCheckService;
import com.gradlemedium200.service.OrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Main API gateway controller that routes requests to appropriate services.
 * This controller serves as the entry point for external clients and handles
 * routing and orchestration of requests across multiple internal services.
 */
@RestController
@RequestMapping("/api/v1")
public class ApiGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);
    
    private final OrchestrationService orchestrationService;
    private final HealthCheckService healthCheckService;
    
    @Autowired
    public ApiGatewayController(OrchestrationService orchestrationService,
                                HealthCheckService healthCheckService) {
        this.orchestrationService = orchestrationService;
        this.healthCheckService = healthCheckService;
    }
    
    /**
     * Gateway endpoint for retrieving users from UserService with pagination
     * 
     * @param page page number (zero-based)
     * @param size number of items per page
     * @return ResponseEntity containing ApiResponse with user data
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("API Gateway: Received request to get users - page: {}, size: {}", page, size);
        
        try {
            // Input validation
            if (page < 0) {
                logger.warn("Invalid page number: {}", page);
                return ResponseEntity.badRequest().body(ApiResponse.error("Page number cannot be negative"));
            }
            
            if (size <= 0 || size > 100) {
                logger.warn("Invalid page size: {}", size);
                return ResponseEntity.badRequest().body(ApiResponse.error("Page size must be between 1 and 100"));
            }
            
            Map<String, Object> result = orchestrationService.getUsers(page, size);
            
            // Check if there was an error retrieving users
            if (result.containsKey("error")) {
                logger.error("Error retrieving users: {}", result.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(result.get("error").toString()));
            }
            
            // Extract pagination info
            Integer totalItems = (Integer) result.getOrDefault("totalItems", 0);
            Integer totalPages = (Integer) result.getOrDefault("totalPages", 0);
            
            // Remove pagination metadata from the data map to avoid duplication
            result.remove("totalItems");
            result.remove("totalPages");
            result.remove("page");
            
            return ResponseEntity.ok(ApiResponse.success(result, totalItems, page, totalPages));
            
        } catch (Exception e) {
            logger.error("Exception while processing getUsers request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error processing user request: " + e.getMessage()));
        }
    }
    
    /**
     * Gateway endpoint for retrieving products from ProductCatalog by category with pagination
     * 
     * @param category product category (optional)
     * @param page page number (zero-based)
     * @return ResponseEntity containing ApiResponse with product data
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page) {
        
        logger.info("API Gateway: Received request to get products - category: {}, page: {}", 
                category != null ? category : "all", page);
        
        try {
            // Input validation
            if (page < 0) {
                logger.warn("Invalid page number: {}", page);
                return ResponseEntity.badRequest().body(ApiResponse.error("Page number cannot be negative"));
            }
            
            Map<String, Object> result = orchestrationService.getProducts(category, page);
            
            // Check if there was an error retrieving products
            if (result.containsKey("error")) {
                logger.error("Error retrieving products: {}", result.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(result.get("error").toString()));
            }
            
            // Extract pagination info
            Integer totalItems = (Integer) result.getOrDefault("totalItems", 0);
            Integer totalPages = (Integer) result.getOrDefault("totalPages", 0);
            
            // Remove pagination metadata from the data map to avoid duplication
            result.remove("totalItems");
            result.remove("totalPages");
            result.remove("page");
            
            return ResponseEntity.ok(ApiResponse.success(result, totalItems, page, totalPages));
            
        } catch (Exception e) {
            logger.error("Exception while processing getProducts request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error processing product request: " + e.getMessage()));
        }
    }
    
    /**
     * Gateway endpoint for retrieving orders from OrderService by user ID
     * 
     * @param userId ID of the user whose orders to retrieve
     * @return ResponseEntity containing ApiResponse with order data
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getOrders(@RequestParam String userId) {
        
        logger.info("API Gateway: Received request to get orders for user: {}", userId);
        
        try {
            // Input validation
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Invalid user ID: empty or null");
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID cannot be empty"));
            }
            
            Map<String, Object> result = orchestrationService.getOrders(userId);
            
            // Check if there was an error retrieving orders
            if (result.containsKey("error")) {
                String errorMessage = result.get("error").toString();
                logger.error("Error retrieving orders: {}", errorMessage);
                
                // Check if it's a "User not found" error
                if ("User not found".equals(errorMessage)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(errorMessage));
                }
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(errorMessage));
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("Exception while processing getOrders request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error processing order request: " + e.getMessage()));
        }
    }
    
    /**
     * Gateway endpoint for overall system health check
     * 
     * @return ResponseEntity containing HealthStatus with system health information
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> getHealth() {
        
        logger.info("API Gateway: Received request to check system health");
        
        try {
            HealthStatus healthStatus = healthCheckService.checkOverallHealth();
            
            // Set HTTP status based on overall health status
            HttpStatus httpStatus;
            
            switch (healthStatus.getOverallStatus()) {
                case UP:
                    httpStatus = HttpStatus.OK;
                    break;
                case DEGRADED:
                    httpStatus = HttpStatus.OK; // Still 200 but with degraded status in body
                    break;
                case DOWN:
                    httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
                    break;
                default:
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            
            return ResponseEntity.status(httpStatus).body(healthStatus);
            
        } catch (Exception e) {
            logger.error("Exception while processing health check request", e);
            
            // Create a minimal health status response for the error case
            HealthStatus errorStatus = new HealthStatus(HealthStatus.Status.DOWN);
            errorStatus.addComponent("apiGateway", HealthStatus.Status.DOWN, 
                    "Error processing health check: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStatus);
        }
    }
    
    /**
     * Fallback handler for undefined endpoints
     * 
     * @return ResponseEntity with error message
     */
    @RequestMapping("/**")
    public ResponseEntity<ApiResponse> handleUndefinedEndpoints() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found or not implemented"));
    }
}