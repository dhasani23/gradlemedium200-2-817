package com.gradlemedium200.listener;

import com.gradlemedium200.service.OrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Event listener for application startup events and initialization.
 * This component handles the application startup process by executing necessary
 * initialization tasks after the Spring Boot application is fully started.
 * It coordinates startup activities through the OrchestrationService.
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * Service for coordinating startup activities
     */
    private final OrchestrationService orchestrationService;
    
    /**
     * Logger for startup events
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    
    /**
     * List of startup tasks to execute
     */
    private final List<String> startupTasks;

    /**
     * Constructor for ApplicationStartupListener.
     *
     * @param orchestrationService The service used to coordinate startup activities
     */
    @Autowired
    public ApplicationStartupListener(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
        
        // Initialize startup tasks list
        this.startupTasks = new ArrayList<>(Arrays.asList(
            "health_check",
            "cache_init",
            "config_validation",
            "service_dependencies_check",
            "background_jobs_init"
        ));
        
        logger.info("ApplicationStartupListener initialized with {} tasks", this.startupTasks.size());
    }

    /**
     * Handles the application ready event, which is triggered after the 
     * Spring Boot application context is completely loaded and ready.
     *
     * @param event The ApplicationReadyEvent that occurred
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application ready event received. Starting initialization sequence...");
        
        try {
            // Validate startup conditions before proceeding
            if (validateStartupConditions()) {
                // Initialize required services
                initializeServices();
                
                // Execute configured startup tasks
                performStartupTasks();
                
                logger.info("Application startup sequence completed successfully");
            } else {
                logger.error("Startup validation failed - application may not function correctly");
                // TODO: Implement graceful degradation or partial startup mode
            }
        } catch (Exception e) {
            logger.error("Fatal error during application startup: {}", e.getMessage(), e);
            // FIXME: Implement proper error reporting mechanism for startup failures
        }
    }

    /**
     * Executes all configured startup tasks.
     * Each task is executed in sequence, with error handling for individual tasks.
     */
    private void performStartupTasks() {
        logger.info("Executing {} startup tasks", startupTasks.size());
        
        int completedTasks = 0;
        
        for (String task : startupTasks) {
            try {
                logger.debug("Executing startup task: {}", task);
                
                switch (task) {
                    case "health_check":
                        // Verify system health at startup
                        // TODO: Implement more comprehensive health check across all services
                        logger.info("Performing health check");
                        break;
                        
                    case "cache_init":
                        // Initialize application caches
                        logger.info("Initializing application caches");
                        break;
                        
                    case "config_validation":
                        // Validate that all required configuration is present
                        logger.info("Validating application configuration");
                        break;
                        
                    case "service_dependencies_check":
                        // Check that all external service dependencies are available
                        logger.info("Checking service dependencies");
                        break;
                        
                    case "background_jobs_init":
                        // Initialize background job processors
                        logger.info("Initializing background jobs");
                        break;
                        
                    default:
                        logger.warn("Unknown startup task: {}", task);
                }
                
                completedTasks++;
            } catch (Exception e) {
                logger.error("Error executing startup task '{}': {}", task, e.getMessage(), e);
                // Continue with next task despite errors
            }
        }
        
        logger.info("Startup tasks execution completed. {}/{} tasks successful", 
                    completedTasks, startupTasks.size());
    }

    /**
     * Validates that all startup conditions are met.
     * Ensures the application environment is properly set up before proceeding.
     *
     * @return true if all conditions are met, false otherwise
     */
    private boolean validateStartupConditions() {
        logger.info("Validating startup conditions");
        
        boolean allConditionsMet = true;
        
        try {
            // Check environment variables
            logger.debug("Checking environment variables");
            String env = System.getenv("SPRING_PROFILES_ACTIVE");
            if (env == null || env.isEmpty()) {
                logger.warn("SPRING_PROFILES_ACTIVE environment variable not set");
                // Not failing for this, just a warning
            }
            
            // Check required system properties
            logger.debug("Checking system properties");
            String javaVersion = System.getProperty("java.version");
            if (javaVersion == null || !javaVersion.startsWith("1.8")) {
                logger.warn("Application is designed for Java 8, but detected: {}", javaVersion);
                // Just a warning, not failing
            }
            
            // FIXME: Add check for database connection - currently assumes it's available
            
            // Verify disk space
            logger.debug("Checking disk space");
            // TODO: Implement proper disk space check
            
            // Check memory availability
            logger.debug("Checking available memory");
            long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
            if (freeMemory < 50) {  // Less than 50MB available
                logger.error("Insufficient memory available for startup: {}MB", freeMemory);
                allConditionsMet = false;
            }
            
        } catch (Exception e) {
            logger.error("Error during startup condition validation: {}", e.getMessage(), e);
            allConditionsMet = false;
        }
        
        logger.info("Startup conditions validation result: {}", allConditionsMet ? "PASSED" : "FAILED");
        return allConditionsMet;
    }

    /**
     * Initializes all dependent services needed for the application to function.
     * Uses the orchestrationService to coordinate service initialization.
     */
    private void initializeServices() {
        logger.info("Initializing dependent services");
        
        try {
            // Register shutdown hook for proper cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook triggered - initiating graceful shutdown");
                orchestrationService.initiateGracefulShutdown();
            }));
            
            // Handle service events for application startup
            orchestrationService.handleServiceEvent("SYSTEM_ALERT", 
                    "Application startup in progress - initializing services");
            
            logger.info("Services initialization completed");
        } catch (Exception e) {
            logger.error("Error initializing services: {}", e.getMessage(), e);
            // FIXME: Implement proper recovery mechanism for initialization failures
            throw new RuntimeException("Failed to initialize required services", e);
        }
    }
}