package com.gradlemedium200.integration;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.gradlemedium200.GradleMedium200Application;
import com.gradlemedium200.dto.ApiResponse;
import com.gradlemedium200.dto.HealthStatus;

/**
 * Integration test for the entire application functionality.
 * This class tests the application's end-to-end functionality by making requests to the
 * actual running application through its REST API endpoints.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = GradleMedium200Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ApplicationIntegrationTest {

    /**
     * Test REST template for integration testing
     */
    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * Random port for test server
     */
    @LocalServerPort
    private int port;

    /**
     * Base URL for test requests
     */
    private String baseUrl;

    @Before
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api/v1";
    }

    /**
     * Tests that application starts up correctly
     */
    @Test
    public void testApplicationStartup() {
        // The fact that this test runs means the Spring context loaded successfully
        // Let's verify we can access the root endpoint
        ResponseEntity<String> response = testRestTemplate.getForEntity("/", String.class);
        
        // This should redirect to a login page or return 404 if no root mapping,
        // but should not be a server error
        assertThat(response.getStatusCode(), not(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Tests health check endpoint functionality
     */
    @Test
    public void testHealthEndpoint() {
        ResponseEntity<HealthStatus> response = testRestTemplate.getForEntity(
                baseUrl + "/health", HealthStatus.class);
        
        // Verify response status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify response body
        HealthStatus healthStatus = response.getBody();
        assertNotNull("Health status should not be null", healthStatus);
        
        // Overall status should not be DOWN
        assertNotEquals("Overall health should not be DOWN", 
                HealthStatus.Status.DOWN, healthStatus.getOverallStatus());
        
        // We should have component statuses
        assertNotNull("Component statuses should not be null", healthStatus.getComponents());
        assertFalse("Component statuses should not be empty", healthStatus.getComponents().isEmpty());
        
        // There should be a version
        assertNotNull("Version should not be null", healthStatus.getVersion());
    }

    /**
     * Tests all API gateway endpoints
     */
    @Test
    public void testApiGatewayEndpoints() {
        // Test users endpoint
        ResponseEntity<ApiResponse> usersResponse = testRestTemplate.getForEntity(
                baseUrl + "/users?page=0&size=10", ApiResponse.class);
        
        assertNotNull("Users response should not be null", usersResponse.getBody());
        assertEquals("Users endpoint should return OK", 
                HttpStatus.OK, usersResponse.getStatusCode());
        
        // Test products endpoint
        ResponseEntity<ApiResponse> productsResponse = testRestTemplate.getForEntity(
                baseUrl + "/products", ApiResponse.class);
        
        assertNotNull("Products response should not be null", productsResponse.getBody());
        assertEquals("Products endpoint should return OK", 
                HttpStatus.OK, productsResponse.getStatusCode());
        
        // Test non-existent endpoint to ensure proper error handling
        ResponseEntity<ApiResponse> notFoundResponse = testRestTemplate.getForEntity(
                baseUrl + "/nonexistent-endpoint", ApiResponse.class);
        
        assertEquals("Non-existent endpoint should return 404", 
                HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        
        // Test invalid parameter handling
        ResponseEntity<ApiResponse> invalidParamResponse = testRestTemplate.getForEntity(
                baseUrl + "/users?page=-1", ApiResponse.class);
        
        assertEquals("Invalid parameter should return 400 Bad Request", 
                HttpStatus.BAD_REQUEST, invalidParamResponse.getStatusCode());
        
        // Test orders endpoint with missing required parameter
        ResponseEntity<ApiResponse> missingParamResponse = testRestTemplate.getForEntity(
                baseUrl + "/orders", ApiResponse.class);
        
        assertEquals("Missing required parameter should return 400 Bad Request", 
                HttpStatus.BAD_REQUEST, missingParamResponse.getStatusCode());
    }

    /**
     * Tests integration between different services
     */
    @Test
    public void testCrossServiceIntegration() {
        // Test the order flow which integrates multiple services
        
        // Step 1: Check if user exists (User Service)
        final String testUserId = "test-user-id";
        ResponseEntity<ApiResponse> userCheckResponse = testRestTemplate.getForEntity(
                baseUrl + "/users?page=0&size=1", ApiResponse.class);
        
        assertEquals("User service should be accessible", 
                HttpStatus.OK, userCheckResponse.getStatusCode());
        
        // Step 2: Get products (Product Catalog)
        ResponseEntity<ApiResponse> productsResponse = testRestTemplate.getForEntity(
                baseUrl + "/products?category=electronics", ApiResponse.class);
        
        assertEquals("Product catalog should be accessible", 
                HttpStatus.OK, productsResponse.getStatusCode());
        
        // Step 3: Check orders for the user (Order Service)
        ResponseEntity<ApiResponse> ordersResponse = testRestTemplate.getForEntity(
                baseUrl + "/orders?userId=" + testUserId, ApiResponse.class);
        
        // Note: This might return 404 if the user doesn't exist, which is also valid
        assertTrue("Order service should return valid response", 
                ordersResponse.getStatusCode() == HttpStatus.OK || 
                ordersResponse.getStatusCode() == HttpStatus.NOT_FOUND);
        
        // FIXME: Implement a complete end-to-end test that creates a user, browses products,
        // creates an order, and verifies notification was sent. Currently limited by test data availability.
        
        // TODO: Test integration with notification service once test data is available
    }
}