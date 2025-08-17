package com.gradlemedium200.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Product Catalog Service.
 * This service manages product information, categories, pricing, and product search.
 */
@SpringBootApplication
public class ProductCatalogApp {
    
    /**
     * Main method to start the Product Catalog Service application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogApp.class, args);
    }
}