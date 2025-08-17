package com.gradlemedium200.health;

import com.gradlemedium200.dto.HealthStatus;
import com.gradlemedium200.service.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for overall application health.
 * 
 * This health indicator provides comprehensive health information about the 
 * application and its components. It uses the HealthCheckService to gather
 * detailed health information.
 */
@Component
public class CustomHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CustomHealthIndicator.class);
    
    private final HealthCheckService healthCheckService;
    
    @Value("${health.threshold:0.75}")
    private double healthThreshold;
    
    /**
     * Creates a new instance of CustomHealthIndicator.
     *
     * @param healthCheckService service for comprehensive health checks
     */
    @Autowired
    public CustomHealthIndicator(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }
    
    /**
     * Performs comprehensive health check and returns status.
     *
     * @return HealthStatus object containing status and details
     */
    public HealthStatus checkHealth() {
        try {
            logger.debug("Performing health check in CustomHealthIndicator");
            
            // Get comprehensive health status
            HealthStatus healthStatus = healthCheckService.checkOverallHealth();
            
            // Calculate health score
            double healthScore = calculateHealthScore();
            
            // Add health score to details
            Map<String, Object> healthDetails = new HashMap<>();
            healthDetails.put("healthScore", healthScore);
            
            HealthStatus.Status finalStatus = healthStatus.getOverallStatus();
            
            // If score is below threshold but status is UP, downgrade to DEGRADED
            if (healthScore < healthThreshold && finalStatus == HealthStatus.Status.UP) {
                finalStatus = HealthStatus.Status.DEGRADED;
            }
            
            // Update overall status
            healthStatus.setOverallStatus(finalStatus);
            
            return healthStatus;
            
        } catch (Exception e) {
            logger.error("Error during health check in CustomHealthIndicator", e);
            
            // Return DOWN status on error
            HealthStatus errorStatus = new HealthStatus(HealthStatus.Status.DOWN);
            errorStatus.addComponent("health-check", HealthStatus.Status.DOWN, 
                    "Error during health check: " + e.getMessage());
            return errorStatus;
        }
    }
    
    /**
     * Calculates overall health score based on all components.
     * The score is a value between 0.0 (completely unhealthy) and 
     * 1.0 (completely healthy).
     *
     * @return health score between 0.0 and 1.0
     */
    public double calculateHealthScore() {
        try {
            HealthStatus healthStatus = healthCheckService.checkOverallHealth();
            
            if (healthStatus == null || healthStatus.getComponents() == null || healthStatus.getComponents().isEmpty()) {
                logger.warn("No health components found when calculating health score");
                return 0.0;
            }
            
            Map<String, HealthStatus.ComponentHealth> components = healthStatus.getComponents();
            int totalComponents = components.size();
            double totalScore = 0.0;
            
            for (Map.Entry<String, HealthStatus.ComponentHealth> entry : components.entrySet()) {
                HealthStatus.ComponentHealth componentHealth = entry.getValue();
                
                // Assign scores based on status
                switch (componentHealth.getStatus()) {
                    case UP:
                        totalScore += 1.0;
                        break;
                    case DEGRADED:
                        totalScore += 0.5;
                        break;
                    case DOWN:
                        totalScore += 0.0;
                        break;
                    case UNKNOWN:
                    default:
                        totalScore += 0.25; // UNKNOWN components contribute minimally
                        break;
                }
            }
            
            // Calculate average score
            return totalComponents > 0 ? totalScore / totalComponents : 0.0;
        } catch (Exception e) {
            logger.error("Error calculating health score", e);
            return 0.0; // Return 0 score in case of error
        }
    }
    
    /**
     * Returns detailed health information for all components.
     * This provides a comprehensive view of the health status of
     * all application components and dependencies.
     *
     * @return map containing detailed health information
     */
    public Map<String, Object> getHealthDetails() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            HealthStatus healthStatus = healthCheckService.checkOverallHealth();
            
            if (healthStatus == null) {
                logger.warn("Health status is null when getting health details");
                details.put("error", "Unable to retrieve health status");
                return details;
            }
            
            // Add version info
            details.put("version", healthStatus.getVersion());
            
            // Add timestamp
            details.put("timestamp", healthStatus.getTimestamp());
            
            // Add overall status
            details.put("status", healthStatus.getOverallStatus().toString());
            
            // Process component health details
            if (healthStatus.getComponents() != null) {
                Map<String, Object> components = new HashMap<>();
                
                for (Map.Entry<String, HealthStatus.ComponentHealth> entry : healthStatus.getComponents().entrySet()) {
                    String componentName = entry.getKey();
                    HealthStatus.ComponentHealth health = entry.getValue();
                    
                    Map<String, Object> componentDetails = new HashMap<>();
                    componentDetails.put("status", health.getStatus().toString());
                    componentDetails.put("details", health.getDetails());
                    
                    components.put(componentName, componentDetails);
                }
                
                details.put("components", components);
            }
            
            return details;
        } catch (Exception e) {
            logger.error("Error retrieving health details", e);
            details.put("error", "Error retrieving health details: " + e.getMessage());
            return details;
        }
    }
    
    /**
     * Sets the health threshold for determining overall health.
     * This is primarily used for testing purposes.
     *
     * @param healthThreshold threshold value between 0.0 and 1.0
     */
    public void setHealthThreshold(double healthThreshold) {
        if (healthThreshold < 0.0 || healthThreshold > 1.0) {
            throw new IllegalArgumentException("Health threshold must be between 0.0 and 1.0");
        }
        this.healthThreshold = healthThreshold;
    }
    
    /**
     * Gets the current health threshold value.
     *
     * @return current health threshold
     */
    public double getHealthThreshold() {
        return healthThreshold;
    }
}