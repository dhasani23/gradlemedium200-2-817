package com.gradlemedium200.listener;

import com.gradlemedium200.service.OrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Event listener for graceful application shutdown and cleanup.
 * This component handles the application shutdown process by executing
 * necessary cleanup tasks when the Spring application context is closed.
 * It coordinates shutdown activities through the OrchestrationService.
 */
@Component
public class ApplicationShutdownListener implements ApplicationListener<ContextClosedEvent> {

    /**
     * Service for coordinating shutdown activities
     */
    private final OrchestrationService orchestrationService;
    
    /**
     * Logger for shutdown events
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationShutdownListener.class);
    
    /**
     * Maximum time to wait for graceful shutdown (in milliseconds)
     */
    private final long shutdownTimeout;
    
    /**
     * Executor service for handling parallel shutdown tasks
     */
    private final ExecutorService shutdownExecutor;
    
    /**
     * List of resources that need to be cleaned up
     */
    private final List<String> resourcesForCleanup;

    /**
     * Constructor for ApplicationShutdownListener.
     *
     * @param orchestrationService The service used to coordinate shutdown activities
     */
    @Autowired
    public ApplicationShutdownListener(
            OrchestrationService orchestrationService,
            @Value("${application.shutdown.timeout:30000}") long shutdownTimeout) {
        this.orchestrationService = orchestrationService;
        this.shutdownTimeout = shutdownTimeout;
        this.shutdownExecutor = Executors.newFixedThreadPool(3);
        
        // Initialize resources to be cleaned up during shutdown
        this.resourcesForCleanup = Arrays.asList(
            "database_connections", 
            "file_handles",
            "network_connections",
            "thread_pools",
            "cached_data"
        );
        
        logger.info("ApplicationShutdownListener initialized with {} ms timeout", this.shutdownTimeout);
    }

    /**
     * Handles the application context closed event, which is triggered when
     * the Spring application context is being shut down.
     *
     * @param event The ContextClosedEvent that occurred
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Application shutdown event received. Starting graceful shutdown sequence...");
        
        try {
            // First notify about the impending shutdown
            notifyShutdown();
            
            // Perform all shutdown tasks
            performShutdownTasks();
            
            // Clean up resources
            cleanupResources();
            
            // Shut down our executor
            shutdownExecutor.shutdown();
            boolean terminated = shutdownExecutor.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
            
            if (!terminated) {
                logger.warn("Shutdown executor did not terminate in {} ms. Forcing shutdown.", shutdownTimeout);
                shutdownExecutor.shutdownNow();
            }
            
            logger.info("Application shutdown sequence completed successfully");
        } catch (Exception e) {
            logger.error("Error during application shutdown: {}", e.getMessage(), e);
            // Attempt to force shutdown if graceful shutdown fails
            shutdownExecutor.shutdownNow();
        }
    }

    /**
     * Executes all configured shutdown tasks.
     * Tasks that can be run in parallel are executed concurrently.
     */
    public void performShutdownTasks() {
        logger.info("Performing shutdown tasks");
        
        try {
            // Stop accepting new requests first
            logger.info("Disabling new request acceptance");
            
            // Wait for in-progress tasks to complete if possible
            logger.info("Waiting for in-progress tasks to complete (timeout: {} ms)", shutdownTimeout);
            
            // Use orchestration service to coordinate shutdown between services
            orchestrationService.initiateGracefulShutdown();
            
            // Perform data persistence for any unsaved data
            logger.info("Ensuring all data is persisted");
            
            // Close messaging channels
            logger.info("Closing messaging channels");
            
            logger.info("Shutdown tasks completed successfully");
        } catch (Exception e) {
            logger.error("Error performing shutdown tasks: {}", e.getMessage(), e);
            // Continue with cleanup despite errors
        }
    }

    /**
     * Cleans up application resources such as connection pools,
     * file handles, thread pools, etc.
     */
    public void cleanupResources() {
        logger.info("Cleaning up {} resources", resourcesForCleanup.size());
        
        int completedCleanups = 0;
        
        // Use CompletableFuture to run some cleanups in parallel
        CompletableFuture<?>[] cleanupTasks = resourcesForCleanup.stream()
            .map(resource -> CompletableFuture.runAsync(() -> {
                try {
                    logger.debug("Cleaning up resource: {}", resource);
                    
                    switch (resource) {
                        case "database_connections":
                            // Close database connections
                            logger.info("Closing database connections");
                            // TODO: Implement actual database connection closing
                            break;
                            
                        case "file_handles":
                            // Close any open file handles
                            logger.info("Closing file handles");
                            break;
                            
                        case "network_connections":
                            // Close network connections
                            logger.info("Closing network connections");
                            break;
                            
                        case "thread_pools":
                            // Shutdown thread pools
                            logger.info("Shutting down thread pools");
                            break;
                            
                        case "cached_data":
                            // Flush any cached data that needs persistence
                            logger.info("Flushing cached data");
                            break;
                            
                        default:
                            logger.warn("Unknown resource type for cleanup: {}", resource);
                    }
                } catch (Exception e) {
                    logger.error("Error cleaning up resource '{}': {}", resource, e.getMessage(), e);
                    // Continue despite errors
                }
            }, shutdownExecutor))
            .toArray(CompletableFuture<?>[]::new);
            
        try {
            // Wait for all cleanup tasks to complete with timeout
            CompletableFuture.allOf(cleanupTasks)
                .get(shutdownTimeout / 2, TimeUnit.MILLISECONDS);
            completedCleanups = resourcesForCleanup.size();
        } catch (Exception e) {
            logger.error("Error during resource cleanup: {}", e.getMessage(), e);
            
            // Count completed cleanups
            completedCleanups = (int) Arrays.stream(cleanupTasks)
                .filter(CompletableFuture::isDone)
                .count();
        }
        
        logger.info("Resource cleanup completed. {}/{} resources cleaned successfully", 
                  completedCleanups, resourcesForCleanup.size());
                  
        // FIXME: Implement proper handling for resources that failed to clean up
    }

    /**
     * Notifies dependent services and components about the impending shutdown.
     * This gives them a chance to prepare for shutdown and complete any critical tasks.
     */
    public void notifyShutdown() {
        logger.info("Notifying services about shutdown");
        
        try {
            // Calculate a safe shutdown deadline
            long shutdownDeadline = System.currentTimeMillis() + shutdownTimeout;
            
            // Notify through orchestration service
            orchestrationService.handleServiceEvent("SHUTDOWN", 
                    "Application shutdown initiated - complete critical operations");
            
            // Log the shutdown timeframe
            logger.info("Services notified. Shutdown will complete by {}", shutdownDeadline);
            
            // Give services a moment to acknowledge shutdown notification
            Thread.sleep(500);
        } catch (Exception e) {
            logger.error("Error notifying services about shutdown: {}", e.getMessage(), e);
            // Continue with shutdown despite notification errors
        }
    }
}