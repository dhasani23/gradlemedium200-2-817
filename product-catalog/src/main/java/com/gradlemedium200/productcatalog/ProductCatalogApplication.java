package com.gradlemedium200.productcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Main Spring Boot application class for the Product Catalog module.
 * This class initializes the Spring Boot application and defines configuration beans.
 * 
 * @author gradlemedium200
 * @version 1.0
 */
@SpringBootApplication
@EnableAsync
public class ProductCatalogApplication {

    /**
     * Default constructor
     */
    public ProductCatalogApplication() {
        // Default constructor
    }

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogApplication.class, args);
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application.
     * This allows the frontend applications to communicate with this API.
     * 
     * @return WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow requests from frontend applications
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000", "https://gradlemedium200.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
                
                // Allow requests for public endpoints from any origin
                registry.addMapping("/api/products/public/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET")
                        .maxAge(3600);
                
                // TODO: Add specific CORS settings for production environment
                // FIXME: Consider using environment-specific CORS configurations
            }
        };
    }

    /**
     * Configures asynchronous task executor for background processing tasks.
     * This executor is used for operations such as image processing, 
     * inventory updates, and notification sending.
     *
     * @return Configured TaskExecutor
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configure thread pool parameters
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("product-catalog-");
        
        // Configure graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds((int) TimeUnit.MINUTES.toSeconds(1));
        
        // Error handling for uncaught exceptions in tasks
        executor.setRejectedExecutionHandler((r, e) -> {
            // Log the rejected task
            System.err.println("Task rejected: " + r.toString());
            throw new RuntimeException("Task execution rejected", new Exception("Thread pool exhausted"));
        });
        
        // Initialize the executor
        executor.initialize();
        
        return executor;
    }
}