package com.gradlemedium200.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradlemedium200.dto.ApiResponse;
import com.gradlemedium200.dto.HealthStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test specifically for API gateway functionality.
 * Tests routing to different services and error handling in the API gateway.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiGatewayIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Tests routing to user service endpoints
     * Verifies that requests to the user service are properly routed through the API gateway
     */
    @Test
    public void testUserServiceRouting() throws Exception {
        // Test getting all users with pagination parameters
        MvcResult result = mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        ApiResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ApiResponse.class);
        assertNotNull("Response data should not be null", response.getData());
        
        // Test with invalid pagination parameters
        mockMvc.perform(get("/api/v1/users")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Page number cannot be negative")));
        
        // Test with invalid size parameter
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Page size must be between 1 and 100")));

        // TODO: Add test for specific user retrieval when that endpoint is implemented
    }

    /**
     * Tests routing to product service endpoints
     * Verifies that requests to the product service are properly routed through the API gateway
     */
    @Test
    public void testProductServiceRouting() throws Exception {
        // Test getting all products
        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.success").value(true));

        // Test getting products by category
        mockMvc.perform(get("/api/v1/products")
                .param("category", "electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // Test with invalid page parameter
        mockMvc.perform(get("/api/v1/products")
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Page number cannot be negative")));
    }

    /**
     * Tests routing to order service endpoints
     * Verifies that requests to the order service are properly routed through the API gateway
     */
    @Test
    public void testOrderServiceRouting() throws Exception {
        // Test getting orders for a valid user
        mockMvc.perform(get("/api/v1/orders")
                .param("userId", "user123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.success").value(true));

        // Test with empty user ID
        mockMvc.perform(get("/api/v1/orders")
                .param("userId", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("User ID cannot be empty")));

        // Test with non-existent user ID (assuming the service returns a not found status)
        // This test might need adjustment based on the actual implementation of the OrderService
        mockMvc.perform(get("/api/v1/orders")
                .param("userId", "nonExistentUser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    /**
     * Tests error handling in API gateway
     * Verifies that the API gateway properly handles various error scenarios
     */
    @Test
    public void testErrorHandling() throws Exception {
        // Test accessing undefined endpoint
        mockMvc.perform(get("/api/v1/nonexistent-endpoint")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Endpoint not found or not implemented")));

        // Test health endpoint - expecting a valid response
        MvcResult healthResult = mockMvc.perform(get("/api/v1/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").exists())
                .andReturn();

        HealthStatus healthStatus = objectMapper.readValue(
                healthResult.getResponse().getContentAsString(), HealthStatus.class);
        assertNotNull("Health status should not be null", healthStatus);
        assertNotNull("Component statuses should not be null", healthStatus.getComponentStatuses());

        // FIXME: Consider adding tests for scenarios where backend services are unavailable
        // This would require mockable service clients or circuit breaker simulation
    }
}