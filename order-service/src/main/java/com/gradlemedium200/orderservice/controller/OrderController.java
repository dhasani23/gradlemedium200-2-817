package com.gradlemedium200.orderservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple controller for orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * Get a health check response.
     *
     * @return A health check message
     */
    @GetMapping("/health")
    public String getHealth() {
        return "Order service is healthy!";
    }
}