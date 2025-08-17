package com.gradlemedium200.service;

import com.gradlemedium200.aws.AwsHealthChecker;
import com.gradlemedium200.dto.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service that monitors health of all modules and external dependencies.
 * This service provides health status information about the application and its components.
 */
@Service
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    
    private final ModuleCoordinationService moduleCoordinationService;
    private final AwsHealthChecker awsHealthChecker;
    private final Map<String, HealthStatus> healthCache;
    
    @Value("${application.version:unknown}")
    private String applicationVersion;
    
    @Value("${health.cache.expiry.seconds:60}")
    private long cacheExpirySeconds;
    
    /**
     * Creates a new instance of HealthCheckService.
     *
     * @param moduleCoordinationService service for checking module health
     * @param awsHealthChecker checker for AWS services health
     */
    @Autowired
    public HealthCheckService(ModuleCoordinationService moduleCoordinationService, 
                             AwsHealthChecker awsHealthChecker) {
        this.moduleCoordinationService = moduleCoordinationService;
        this.awsHealthChecker = awsHealthChecker;
        this.healthCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Performs comprehensive health check of all components.
     * This checks the health of all internal modules and external dependencies.
     *
     * @return HealthStatus containing the overall health status of the application
     */
    public HealthStatus checkOverallHealth() {
        logger.debug("Performing comprehensive health check of all components");
        
        // Check for cached result
        HealthStatus cachedStatus = getCachedHealth("overall");
        if (cachedStatus != null) {
            return cachedStatus;
        }
        
        // Create new health status
        HealthStatus healthStatus = new HealthStatus();
        
        try {
            // Check modules health in parallel
            CompletableFuture<HealthStatus> userServiceFuture = CompletableFuture.supplyAsync(() -> 
                    checkModuleHealth("user-service"));
            
            CompletableFuture<HealthStatus> productCatalogFuture = CompletableFuture.supplyAsync(() -> 
                    checkModuleHealth("product-catalog"));
            
            CompletableFuture<HealthStatus> orderServiceFuture = CompletableFuture.supplyAsync(() -> 
                    checkModuleHealth("order-service"));
            
            CompletableFuture<HealthStatus> notificationServiceFuture = CompletableFuture.supplyAsync(() -> 
                    checkModuleHealth("notification-service"));
            
            // Check external dependencies
            CompletableFuture<HealthStatus> externalDependenciesFuture = CompletableFuture.supplyAsync(() -> 
                    checkExternalDependencies());
            
            // Wait for all checks to complete
            CompletableFuture.allOf(
                    userServiceFuture,
                    productCatalogFuture,
                    orderServiceFuture,
                    notificationServiceFuture,
                    externalDependenciesFuture
            ).get(10, TimeUnit.SECONDS);  // Timeout after 10 seconds
            
            // Combine results
            mergeHealthStatus(healthStatus, userServiceFuture.get());
            mergeHealthStatus(healthStatus, productCatalogFuture.get());
            mergeHealthStatus(healthStatus, orderServiceFuture.get());
            mergeHealthStatus(healthStatus, notificationServiceFuture.get());
            mergeHealthStatus(healthStatus, externalDependenciesFuture.get());
            
            // Update overall status
            healthStatus.updateOverallStatus();
            healthStatus.setVersion(applicationVersion);
            
            // Cache the result
            healthCache.put("overall", healthStatus);
            
            return healthStatus;
        } catch (Exception e) {
            logger.error("Error during health check", e);
            
            // Return degraded status in case of error
            healthStatus.setOverallStatus(HealthStatus.Status.DEGRADED);
            healthStatus.addComponent("health-check-system", 
                    HealthStatus.Status.DEGRADED, 
                    "Error during health check execution: " + e.getMessage());
            healthStatus.setVersion(applicationVersion);
            
            return healthStatus;
        }
    }
    
    /**
     * Checks health of a specific module.
     *
     * @param moduleName name of the module to check
     * @return HealthStatus containing the health status of the specified module
     */
    public HealthStatus checkModuleHealth(String moduleName) {
        logger.debug("Checking health of module: {}", moduleName);
        
        // Check for cached result
        HealthStatus cachedStatus = getCachedHealth(moduleName);
        if (cachedStatus != null) {
            return cachedStatus;
        }
        
        HealthStatus healthStatus = new HealthStatus(HealthStatus.Status.UNKNOWN);
        healthStatus.setVersion(applicationVersion);
        
        try {
            switch (moduleName.toLowerCase()) {
                case "user-service":
                    // Check user service health
                    healthStatus.addComponent("user-service", HealthStatus.Status.UP, "User service is operational");
                    // TODO: Implement actual health check for user service
                    break;
                    
                case "product-catalog":
                    // Check product catalog health
                    healthStatus.addComponent("product-catalog", HealthStatus.Status.UP, "Product catalog service is operational");
                    // TODO: Implement actual health check for product catalog
                    break;
                    
                case "order-service":
                    // Check order service health
                    healthStatus.addComponent("order-service", HealthStatus.Status.UP, "Order service is operational");
                    // TODO: Implement actual health check for order service
                    break;
                    
                case "notification-service":
                    // Check notification service health
                    healthStatus.addComponent("notification-service", HealthStatus.Status.UP, "Notification service is operational");
                    // TODO: Implement actual health check for notification service
                    break;
                    
                default:
                    logger.warn("Unknown module name: {}", moduleName);
                    healthStatus.addComponent(moduleName, HealthStatus.Status.UNKNOWN, "Unknown module");
            }
            
            healthStatus.updateOverallStatus();
            
            // Cache the result
            healthCache.put(moduleName, healthStatus);
            
            return healthStatus;
        } catch (Exception e) {
            logger.error("Error checking module health: {}", moduleName, e);
            
            healthStatus.addComponent(moduleName, HealthStatus.Status.DOWN, 
                    "Error checking module health: " + e.getMessage());
            healthStatus.updateOverallStatus();
            
            return healthStatus;
        }
    }
    
    /**
     * Checks health of external dependencies like AWS services.
     *
     * @return HealthStatus containing the health status of external dependencies
     */
    public HealthStatus checkExternalDependencies() {
        logger.debug("Checking health of external dependencies");
        
        // Check for cached result
        HealthStatus cachedStatus = getCachedHealth("external");
        if (cachedStatus != null) {
            return cachedStatus;
        }
        
        try {
            // Get AWS health status
            HealthStatus awsHealthStatus = awsHealthChecker.checkAllServices();
            
            // Add version information
            awsHealthStatus.setVersion(applicationVersion);
            
            // Cache the result
            healthCache.put("external", awsHealthStatus);
            
            return awsHealthStatus;
        } catch (Exception e) {
            logger.error("Error checking external dependencies", e);
            
            // Create error status
            HealthStatus errorStatus = new HealthStatus(HealthStatus.Status.DOWN);
            errorStatus.addComponent("external-dependencies", HealthStatus.Status.DOWN, 
                    "Error checking external dependencies: " + e.getMessage());
            errorStatus.setVersion(applicationVersion);
            
            return errorStatus;
        }
    }
    
    /**
     * Retrieves cached health status for a component.
     * Returns null if no cached value exists or if the cache has expired.
     *
     * @param component the component name
     * @return cached HealthStatus or null if not found or expired
     */
    public HealthStatus getCachedHealth(String component) {
        if (component == null) {
            return null;
        }
        
        HealthStatus status = healthCache.get(component);
        
        if (status == null) {
            return null;
        }
        
        // Check if cache has expired
        long currentTime = System.currentTimeMillis();
        long cacheTime = status.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long expiryTimeMillis = cacheExpirySeconds * 1000;
        
        if (currentTime - cacheTime > expiryTimeMillis) {
            // Cache has expired, remove it
            healthCache.remove(component);
            return null;
        }
        
        // Return cached status
        return status;
    }
    
    /**
     * Invalidates all cached health statuses, forcing fresh health checks.
     */
    public void invalidateCache() {
        logger.debug("Invalidating health check cache");
        healthCache.clear();
    }
    
    /**
     * Invalidates cached health status for a specific component.
     * 
     * @param component component name to invalidate
     */
    public void invalidateComponentCache(String component) {
        logger.debug("Invalidating health check cache for component: {}", component);
        healthCache.remove(component);
    }
    
    /**
     * Merges health status from source into target.
     * 
     * @param target target health status
     * @param source source health status
     */
    private void mergeHealthStatus(HealthStatus target, HealthStatus source) {
        if (target == null || source == null) {
            return;
        }
        
        if (source.getComponents() != null) {
            for (Map.Entry<String, HealthStatus.ComponentHealth> entry : source.getComponents().entrySet()) {
                target.addComponent(
                        entry.getKey(),
                        entry.getValue().getStatus(),
                        entry.getValue().getDetails()
                );
            }
        }
    }
}