package com.gradlemedium200.service;

import com.gradlemedium200.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import com.gradlemedium200.util.MapUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service that orchestrates interactions between different modules and services.
 * Acts as a central coordinator for cross-service operations, ensuring proper
 * sequencing, error handling, and fault tolerance.
 */
@Service
public class OrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationService.class);
    
    private final ModuleCoordinationService moduleCoordinationService;
    private final EventPublisherService eventPublisherService;
    private final CircuitBreaker circuitBreaker;
    private final ExecutorService executorService;
    
    private volatile boolean shutdownInProgress = false;

    /**
     * Constructor for OrchestrationService
     *
     * @param moduleCoordinationService Service for coordinating between modules
     * @param eventPublisherService Service for publishing events
     */
    @Autowired
    public OrchestrationService(ModuleCoordinationService moduleCoordinationService,
                               EventPublisherService eventPublisherService) {
        this.moduleCoordinationService = moduleCoordinationService;
        this.eventPublisherService = eventPublisherService;
        
        // Initialize circuit breaker for fault tolerance
        // Parameters: name, failure threshold, reset timeout in milliseconds
        this.circuitBreaker = new CircuitBreaker("orchestration-service", 5, 30000);
        
        // Thread pool for async operations
        this.executorService = Executors.newCachedThreadPool();
        
        logger.info("OrchestrationService initialized");
    }

    /**
     * Gets users with pagination support.
     *
     * @param page Page number for pagination
     * @param size Size of each page
     * @return Map containing user data and pagination info
     */
    public Map<String, Object> getUsers(int page, int size) {
        logger.debug("Getting users - page: {}, size: {}", page, size);
        
        if (shutdownInProgress) {
            logger.warn("Rejecting user fetch as shutdown is in progress");
            return MapUtil.of("success", false, "message", "Service is shutting down");
        }
        
        return circuitBreaker.execute(() -> {
            try {
                Object userData = moduleCoordinationService.fetchAggregatedData("user_list", 
                        MapUtil.of("page", page, "size", size));
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Users retrieved successfully");
                result.put("data", userData);
                
                return result;
            } catch (Exception e) {
                logger.error("Error retrieving users: {}", e.getMessage(), e);
                return MapUtil.of(
                    "success", false,
                    "message", "Error retrieving users: " + e.getMessage()
                );
            }
        }, () -> MapUtil.of(
            "success", false,
            "message", "User service temporarily unavailable"
        ));
    }
    
    /**
     * Gets products filtered by category with pagination support.
     *
     * @param category Product category filter (can be null)
     * @param page Page number for pagination
     * @return Map containing product data and pagination info
     */
    public Map<String, Object> getProducts(String category, int page) {
        logger.debug("Getting products - category: {}, page: {}", category, page);
        
        if (shutdownInProgress) {
            logger.warn("Rejecting product fetch as shutdown is in progress");
            return MapUtil.of("success", false, "message", "Service is shutting down");
        }
        
        return circuitBreaker.execute(() -> {
            try {
                Map<String, Object> filters = new HashMap<>();
                filters.put("page", page);
                if (category != null && !category.isEmpty()) {
                    filters.put("category", category);
                }
                
                Object productData = moduleCoordinationService.fetchAggregatedData("product_list", filters);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Products retrieved successfully");
                result.put("data", productData);
                
                return result;
            } catch (Exception e) {
                logger.error("Error retrieving products: {}", e.getMessage(), e);
                return MapUtil.of(
                    "success", false,
                    "message", "Error retrieving products: " + e.getMessage()
                );
            }
        }, () -> MapUtil.of(
            "success", false,
            "message", "Product service temporarily unavailable"
        ));
    }
    
    /**
     * Gets orders for a specific user.
     *
     * @param userId ID of the user to get orders for
     * @return Map containing order data
     */
    public Map<String, Object> getOrders(String userId) {
        logger.debug("Getting orders for user: {}", userId);
        
        if (shutdownInProgress) {
            logger.warn("Rejecting order fetch as shutdown is in progress");
            return MapUtil.of("success", false, "message", "Service is shutting down");
        }
        
        return circuitBreaker.execute(() -> {
            try {
                Object orderData = moduleCoordinationService.fetchAggregatedData("user_orders", 
                        MapUtil.of("userId", userId));
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Orders retrieved successfully");
                result.put("data", orderData);
                
                return result;
            } catch (Exception e) {
                logger.error("Error retrieving orders for user {}: {}", userId, e.getMessage(), e);
                return MapUtil.of(
                    "success", false,
                    "message", "Error retrieving orders: " + e.getMessage()
                );
            }
        }, () -> MapUtil.of(
            "success", false,
            "message", "Order service temporarily unavailable"
        ));
    }

    /**
     * Orchestrates user-related requests across multiple services.
     * Routes the request to appropriate handlers and aggregates responses.
     *
     * @param requestType Type of user request (e.g., "profile", "preferences", "auth")
     * @param requestData Request data payload
     * @return ApiResponse containing aggregated response
     */
    public ApiResponse processUserRequest(String requestType, Object requestData) {
        logger.debug("Processing user request of type: {} with data: {}", requestType, requestData);
        
        if (shutdownInProgress) {
            logger.warn("Rejecting user request as shutdown is in progress");
            return new ApiResponse(false, "Service is shutting down", null);
        }
        
        return circuitBreaker.executeApiResponse(() -> {
            try {
                // Validate request
                if (requestType == null || requestData == null) {
                    return new ApiResponse(false, "Invalid request parameters", null);
                }
                
                // Route based on request type
                switch (requestType.toLowerCase()) {
                    case "profile":
                        return moduleCoordinationService.processUserProfileRequest(requestData);
                        
                    case "preferences":
                        return moduleCoordinationService.processUserPreferencesRequest(requestData);
                        
                    case "auth":
                        // Authentication requests need special handling and may involve multiple services
                        ApiResponse authResult = moduleCoordinationService.processAuthenticationRequest(requestData);
                        
                        // Publish auth event if successful
                        if (authResult.isSuccess()) {
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("type", "USER_AUTHENTICATED");
                            eventData.put("userData", authResult.getData());
                            eventPublisherService.publishEvent("user-events", eventData);
                        }
                        
                        return authResult;
                        
                    default:
                        logger.warn("Unknown user request type: {}", requestType);
                        return new ApiResponse(false, "Unknown request type", null);
                }
            } catch (Exception e) {
                logger.error("Error processing user request: {}", e.getMessage(), e);
                return new ApiResponse(false, "Error processing request: " + e.getMessage(), null);
            }
        }, () -> new ApiResponse(false, "Service temporarily unavailable", null));
    }

    /**
     * Orchestrates the complete order workflow across services.
     * Coordinates the entire order process from validation to fulfillment.
     *
     * @param orderData Order data containing product info, quantities, customer details, etc.
     * @return ApiResponse with order processing result
     */
    public ApiResponse processOrderWorkflow(Object orderData) {
        logger.debug("Processing order workflow with data: {}", orderData);
        
        if (shutdownInProgress) {
            logger.warn("Rejecting order workflow as shutdown is in progress");
            return new ApiResponse(false, "Service is shutting down", null);
        }
        
        return circuitBreaker.executeApiResponse(() -> {
            try {
                // 1. Validate order data
                ApiResponse validationResponse = moduleCoordinationService.validateOrderData(orderData);
                if (!validationResponse.isSuccess()) {
                    return validationResponse;
                }
                
                // 2. Check inventory availability
                ApiResponse inventoryResponse = moduleCoordinationService.checkInventoryAvailability(orderData);
                if (!inventoryResponse.isSuccess()) {
                    return inventoryResponse;
                }
                
                // 3. Process payment - critical step
                ApiResponse paymentResponse = moduleCoordinationService.processPayment(orderData);
                if (!paymentResponse.isSuccess()) {
                    // Payment failed, publish event for monitoring
                    eventPublisherService.publishEvent("payment-failures", 
                            MapUtil.of("orderId", orderData.toString(), "reason", paymentResponse.getMessage()));
                    return paymentResponse;
                }
                
                // 4. Create order record
                ApiResponse orderCreationResponse = moduleCoordinationService.createOrder(orderData);
                if (!orderCreationResponse.isSuccess()) {
                    // Critical failure after payment - needs manual intervention
                    // FIXME: Implement compensation transaction for payment reversal
                    logger.error("Payment succeeded but order creation failed. Manual intervention needed!");
                    eventPublisherService.publishEvent("critical-failures", 
                            MapUtil.of("type", "ORDER_CREATION_AFTER_PAYMENT", "orderData", orderData));
                    return new ApiResponse(false, "Critical error in order processing. Payment was processed but order recording failed.", null);
                }
                
                // 5. Update inventory
                // TODO: Make this step eventually consistent to avoid blocking the response
                moduleCoordinationService.updateInventory(orderData);
                
                // 6. Send notifications asynchronously
                CompletableFuture.runAsync(() -> {
                    try {
                        moduleCoordinationService.sendOrderNotifications(orderData);
                    } catch (Exception e) {
                        logger.error("Failed to send order notifications", e);
                    }
                }, executorService);
                
                // 7. Return success
                logger.info("Order workflow completed successfully");
                return new ApiResponse(true, "Order processed successfully", orderCreationResponse.getData());
                
            } catch (Exception e) {
                logger.error("Error in order workflow: {}", e.getMessage(), e);
                return new ApiResponse(false, "Order processing failed: " + e.getMessage(), null);
            }
        }, () -> new ApiResponse(false, "Order service temporarily unavailable", null));
    }

    /**
     * Handles events from various services and coordinates responses.
     * Acts as an event router for inter-service communication.
     *
     * @param eventType Type of event to handle
     * @param eventData Event data payload
     */
    public void handleServiceEvent(String eventType, Object eventData) {
        logger.debug("Handling service event of type: {} with data: {}", eventType, eventData);
        
        if (shutdownInProgress && !eventType.equals("SHUTDOWN")) {
            logger.warn("Ignoring non-shutdown event as shutdown is in progress");
            return;
        }
        
        try {
            switch (eventType) {
                case "INVENTORY_CHANGE":
                    // Update inventory status and notify affected services
                    moduleCoordinationService.handleInventoryChangeEvent(eventData);
                    break;
                    
                case "USER_STATUS_CHANGE":
                    // Handle user status changes (active, suspended, etc.)
                    moduleCoordinationService.handleUserStatusChangeEvent(eventData);
                    // Propagate user status events to other services that need to know
                    eventPublisherService.publishEvent("user-status-updates", eventData);
                    break;
                    
                case "PAYMENT_RESULT":
                    // Process payment results and trigger appropriate workflows
                    moduleCoordinationService.handlePaymentResultEvent(eventData);
                    break;
                    
                case "SYSTEM_ALERT":
                    // Handle system alerts like resource constraints, security issues
                    handleSystemAlert(eventData);
                    break;
                    
                case "SHUTDOWN":
                    // Handle shutdown event
                    initiateGracefulShutdown();
                    break;
                    
                default:
                    logger.warn("Unknown event type received: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error handling service event {}: {}", eventType, e.getMessage(), e);
            
            // Record failure in circuit breaker if it's a critical service
            if (isCriticalEventType(eventType)) {
                circuitBreaker.recordFailure();
            }
            
            // Publish error event for monitoring
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("originalEventType", eventType);
            errorData.put("errorMessage", e.getMessage());
            eventPublisherService.publishEvent("service-errors", errorData);
        }
    }

    /**
     * Initiates graceful shutdown procedure for all services.
     * Ensures that in-progress operations complete and resources are released properly.
     */
    public void initiateGracefulShutdown() {
        logger.info("Initiating graceful shutdown of services");
        
        // Mark as shutting down to prevent new requests
        shutdownInProgress = true;
        
        try {
            // 1. Publish shutdown event to all services
            eventPublisherService.publishEvent("system", MapUtil.of("type", "PREPARE_SHUTDOWN"));
            
            // 2. Wait for in-progress operations to complete (with timeout)
            logger.info("Waiting for in-progress operations to complete...");
            boolean terminatedCleanly = executorService.awaitTermination(30, TimeUnit.SECONDS);
            if (!terminatedCleanly) {
                logger.warn("Some tasks did not complete during graceful shutdown period");
            }
            
            // 3. Coordinate module-specific shutdown procedures
            moduleCoordinationService.initiateModulesShutdown();
            
            // 4. Release resources
            logger.info("Shutting down executor service");
            executorService.shutdown();
            
            logger.info("Graceful shutdown completed");
        } catch (Exception e) {
            logger.error("Error during graceful shutdown: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup method that runs when the service is being destroyed
     */
    @PreDestroy
    public void cleanup() {
        if (!shutdownInProgress) {
            initiateGracefulShutdown();
        }
    }
    
    /**
     * Handle system alerts such as resource constraints or security issues
     *
     * @param alertData Alert data containing details about the system issue
     */
    private void handleSystemAlert(Object alertData) {
        logger.warn("System alert received: {}", alertData);
        
        // TODO: Implement proper system alert handling with different severity levels
        
        // For critical alerts, we might need to take immediate action
        if (alertData.toString().contains("CRITICAL")) {
            logger.error("Critical system alert! Taking protective measures");
            // Example: Reset circuit breaker to be more conservative
            circuitBreaker.reset();
        }
    }
    
    /**
     * Determines if an event type is considered critical for circuit breaking
     *
     * @param eventType The event type to check
     * @return true if the event is critical, false otherwise
     */
    private boolean isCriticalEventType(String eventType) {
        return eventType.equals("PAYMENT_RESULT") || 
               eventType.equals("INVENTORY_CHANGE") || 
               eventType.equals("SYSTEM_ALERT");
    }
}