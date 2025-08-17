package com.gradlemedium200.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the User Service.
 * This service handles user authentication, authorization, and user management.
 */
@SpringBootApplication
public class UserServiceApp {
    
    /**
     * Main method to start the User Service application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApp.class, args);
    }
}