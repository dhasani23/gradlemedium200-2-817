package com.gradlemedium200.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Order Service.
 * This service handles order creation, order lifecycle management, payment processing, and order fulfillment.
 */
@SpringBootApplication
public class OrderServiceApp {
    
    /**
     * Main method to start the Order Service application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApp.class, args);
    }
}