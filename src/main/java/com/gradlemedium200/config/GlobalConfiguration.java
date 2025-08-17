package com.gradlemedium200.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.Collections;

/**
 * Global configuration class for application-wide settings and bean definitions.
 * This class provides configuration beans that are used across the entire application
 * including ObjectMapper for JSON serialization/deserialization, thread pool configuration
 * for asynchronous operations, and CORS settings for cross-origin requests.
 *
 * @author GradleMedium200 Team
 */
@Configuration
public class GlobalConfiguration {

    @Value("${application.name:GradleMedium200}")
    private String applicationName;

    @Value("${application.version:1.0.0}")
    private String applicationVersion;

    /**
     * Creates and configures ObjectMapper bean for JSON processing.
     * This bean is used throughout the application for consistent JSON handling.
     * 
     * Features:
     * - Proper date/time handling via JavaTimeModule
     * - Indentation in development environments
     * - Customized serialization settings
     *
     * @return Configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register JavaTimeModule for proper date/time handling
        objectMapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps (use ISO-8601 format instead)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Enable pretty-printing for better readability in development
        // TODO: Disable in production to reduce response size
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // FIXME: Add more configuration settings for handling empty beans and unknown properties
        
        return objectMapper;
    }

    /**
     * Creates thread pool task executor for async operations.
     * This executor is used for handling asynchronous tasks throughout the application,
     * ensuring efficient resource utilization and improved performance for non-blocking operations.
     *
     * @return Configured ThreadPoolTaskExecutor
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configure thread pool parameters
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix(applicationName + "-thread-");
        
        // Set rejection policy
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Initialize the executor
        executor.initialize();
        
        return executor;
    }

    /**
     * Configures CORS settings for cross-origin requests.
     * This allows the frontend applications to communicate with the backend API
     * from different origins, with appropriate security restrictions.
     *
     * @return Configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins or all origins during development
        // TODO: Restrict to specific origins in production
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "X-Auth-Token",
            "X-Api-Version"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Set max age for preflight requests
        configuration.setMaxAge(3600L);
        
        // Add configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Gets the application name from properties
     * 
     * @return The application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Gets the application version from properties
     * 
     * @return The application version
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }
}