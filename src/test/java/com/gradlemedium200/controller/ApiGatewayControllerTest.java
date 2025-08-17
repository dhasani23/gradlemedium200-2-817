package com.gradlemedium200.controller;

import com.gradlemedium200.dto.ApiResponse;
import com.gradlemedium200.dto.HealthStatus;
import com.gradlemedium200.service.HealthCheckService;
import com.gradlemedium200.service.OrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ApiGatewayController functionality.
 * Tests the key endpoints including user retrieval, product retrieval,
 * order retrieval, and health check.
 */
@ExtendWith(MockitoExtension.class)
public class ApiGatewayControllerTest {

    @InjectMocks
    private ApiGatewayController apiGatewayController;

    @Mock
    private OrchestrationService orchestrationService;

    @Mock
    private HealthCheckService healthCheckService;

    /**
     * Setup test environment before each test.
     */
    @BeforeEach
    public void setup() {
        // No additional setup required as we're using MockitoExtension
    }

    /**
     * Tests user retrieval endpoint to verify proper handling of
     * pagination and response formatting.
     */
    @Test
    public void testGetUsers() {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("users", new Object[] { /* mock user data */ });
        mockResult.put("totalItems", 25);
        mockResult.put("totalPages", 3);
        
        when(orchestrationService.getUsers(anyInt(), anyInt())).thenReturn(mockResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getUsers(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        // Verify the orchestration service was called with correct parameters
        verify(orchestrationService).getUsers(0, 10);
        
        // Verify pagination info is passed through correctly
        assertEquals(25, response.getBody().getTotalItems());
        assertEquals(3, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getPage());
    }

    /**
     * Tests the error handling scenario for user retrieval when 
     * invalid pagination parameters are provided.
     */
    @Test
    public void testGetUsers_WithInvalidPageNumber() {
        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getUsers(-1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Page number cannot be negative"));
        
        // Verify orchestration service was NOT called
        verify(orchestrationService, never()).getUsers(anyInt(), anyInt());
    }

    /**
     * Tests the error handling scenario for user retrieval when 
     * the orchestration service encounters an error.
     */
    @Test
    public void testGetUsers_WithServiceError() {
        // Arrange
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "Database connection failed");
        
        when(orchestrationService.getUsers(anyInt(), anyInt())).thenReturn(errorResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getUsers(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Database connection failed", response.getBody().getMessage());
    }

    /**
     * Tests product retrieval endpoint to verify proper handling of
     * category filtering and pagination.
     */
    @Test
    public void testGetProducts() {
        // Arrange
        String category = "electronics";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("products", new Object[] { /* mock product data */ });
        mockResult.put("totalItems", 15);
        mockResult.put("totalPages", 2);
        
        when(orchestrationService.getProducts(eq(category), anyInt())).thenReturn(mockResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getProducts(category, 0);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        // Verify the orchestration service was called with correct parameters
        verify(orchestrationService).getProducts(category, 0);
        
        // Verify pagination info is passed through correctly
        assertEquals(15, response.getBody().getTotalItems());
        assertEquals(2, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getPage());
    }

    /**
     * Tests product retrieval endpoint with no category filter.
     */
    @Test
    public void testGetProducts_WithoutCategory() {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("products", new Object[] { /* mock product data */ });
        mockResult.put("totalItems", 30);
        mockResult.put("totalPages", 3);
        
        when(orchestrationService.getProducts(isNull(), anyInt())).thenReturn(mockResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getProducts(null, 0);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        // Verify the orchestration service was called with correct parameters
        verify(orchestrationService).getProducts(null, 0);
        
        // Verify pagination info is passed through correctly
        assertEquals(30, response.getBody().getTotalItems());
        assertEquals(3, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getPage());
    }

    /**
     * Tests the error handling scenario for product retrieval when 
     * invalid pagination parameters are provided.
     */
    @Test
    public void testGetProducts_WithInvalidPageNumber() {
        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getProducts("electronics", -1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Page number cannot be negative"));
        
        // Verify orchestration service was NOT called
        verify(orchestrationService, never()).getProducts(anyString(), anyInt());
    }

    /**
     * Tests order retrieval endpoint to verify proper handling of
     * user ID parameter and response formatting.
     */
    @Test
    public void testGetOrders() {
        // Arrange
        String userId = "user123";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("orders", new Object[] { /* mock order data */ });
        
        when(orchestrationService.getOrders(eq(userId))).thenReturn(mockResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getOrders(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        // Verify the orchestration service was called with correct parameters
        verify(orchestrationService).getOrders(userId);
    }

    /**
     * Tests the error handling scenario for order retrieval when 
     * an empty user ID is provided.
     */
    @Test
    public void testGetOrders_WithEmptyUserId() {
        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getOrders("");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("User ID cannot be empty"));
        
        // Verify orchestration service was NOT called
        verify(orchestrationService, never()).getOrders(anyString());
    }

    /**
     * Tests the error handling scenario for order retrieval when
     * the user doesn't exist.
     */
    @Test
    public void testGetOrders_WithNonExistentUser() {
        // Arrange
        String userId = "nonexistent";
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "User not found");
        
        when(orchestrationService.getOrders(eq(userId))).thenReturn(errorResult);

        // Act
        ResponseEntity<ApiResponse> response = apiGatewayController.getOrders(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    /**
     * Tests health check endpoint to verify proper response formatting
     * and status code for healthy system.
     */
    @Test
    public void testGetHealth() {
        // Arrange
        HealthStatus healthStatus = new HealthStatus(HealthStatus.Status.UP);
        healthStatus.addComponent("api-gateway", HealthStatus.Status.UP, "API Gateway is operational");
        healthStatus.addComponent("user-service", HealthStatus.Status.UP, "User service is operational");
        
        when(healthCheckService.checkOverallHealth()).thenReturn(healthStatus);

        // Act
        ResponseEntity<HealthStatus> response = apiGatewayController.getHealth();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HealthStatus.Status.UP, response.getBody().getOverallStatus());
        
        // Verify the health check service was called
        verify(healthCheckService).checkOverallHealth();
    }

    /**
     * Tests health check endpoint with degraded system health.
     */
    @Test
    public void testGetHealth_WithDegradedSystem() {
        // Arrange
        HealthStatus healthStatus = new HealthStatus(HealthStatus.Status.DEGRADED);
        healthStatus.addComponent("api-gateway", HealthStatus.Status.UP, "API Gateway is operational");
        healthStatus.addComponent("user-service", HealthStatus.Status.DEGRADED, "User service experiencing high latency");
        
        when(healthCheckService.checkOverallHealth()).thenReturn(healthStatus);

        // Act
        ResponseEntity<HealthStatus> response = apiGatewayController.getHealth();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Still 200 even when degraded
        assertNotNull(response.getBody());
        assertEquals(HealthStatus.Status.DEGRADED, response.getBody().getOverallStatus());
        
        // Verify components are correctly included
        assertNotNull(response.getBody().getComponents());
        assertTrue(response.getBody().getComponents().containsKey("user-service"));
        assertEquals(HealthStatus.Status.DEGRADED, 
                response.getBody().getComponents().get("user-service").getStatus());
    }

    /**
     * Tests health check endpoint with system down.
     */
    @Test
    public void testGetHealth_WithSystemDown() {
        // Arrange
        HealthStatus healthStatus = new HealthStatus(HealthStatus.Status.DOWN);
        healthStatus.addComponent("api-gateway", HealthStatus.Status.UP, "API Gateway is operational");
        healthStatus.addComponent("user-service", HealthStatus.Status.DOWN, "User service is not responding");
        
        when(healthCheckService.checkOverallHealth()).thenReturn(healthStatus);

        // Act
        ResponseEntity<HealthStatus> response = apiGatewayController.getHealth();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode()); // 503 when system is down
        assertNotNull(response.getBody());
        assertEquals(HealthStatus.Status.DOWN, response.getBody().getOverallStatus());
    }

    /**
     * Tests health check endpoint when an exception occurs during health check.
     */
    @Test
    public void testGetHealth_WithException() {
        // Arrange
        when(healthCheckService.checkOverallHealth()).thenThrow(new RuntimeException("Health check failed"));

        // Act
        ResponseEntity<HealthStatus> response = apiGatewayController.getHealth();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HealthStatus.Status.DOWN, response.getBody().getOverallStatus());
        
        // Verify error component is included
        assertNotNull(response.getBody().getComponents());
        assertTrue(response.getBody().getComponents().containsKey("apiGateway"));
        assertEquals(HealthStatus.Status.DOWN, 
                response.getBody().getComponents().get("apiGateway").getStatus());
    }
}