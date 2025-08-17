package com.gradlemedium200.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Notification Service.
 * This service handles email notifications, SMS notifications, push notifications, and in-app notifications.
 */
@SpringBootApplication
public class NotificationServiceApp {
    
    /**
     * Main method to start the Notification Service application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApp.class, args);
    }
}