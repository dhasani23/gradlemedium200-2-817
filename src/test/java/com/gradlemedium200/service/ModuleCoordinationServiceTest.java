package com.gradlemedium200.service;

import com.gradlemedium200.client.NotificationServiceClient;
import com.gradlemedium200.client.OrderServiceClient;
import com.gradlemedium200.client.ProductCatalogClient;
import com.gradlemedium200.client.UserServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ModuleCoordinationService functionality.
 * Tests focus on the service's coordination capabilities between different
 * microservices in the system.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleCoordinationServiceTest {

    private ModuleCoordinationService moduleCoordinationService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ProductCatalogClient productCatalogClient;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Before
    public void setUp() {
        moduleCoordinationService = new ModuleCoordinationService(
                userServiceClient,
                productCatalogClient,
                orderServiceClient,
                notificationServiceClient);
    }

    /**
     * Tests user registration coordination functionality including:
     * 1. User creation through user service
     * 2. Preference initialization through product catalog service
     * 3. Welcome notification sending
     */
    @Test
    public void testCoordinateUserRegistration() {
        // Prepare test data
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testuser");
        userData.put("email", "testuser@example.com");
        userData.put("firstName", "Test");
        userData.put("lastName", "User");

        Map<String, Object> userCreationResult = new HashMap<>();
        userCreationResult.put("id", "user-123");
        userCreationResult.put("username", "testuser");
        userCreationResult.put("status", "ACTIVE");

        Map<String, Object> preferenceResult = new HashMap<>();
        preferenceResult.put("userId", "user-123");
        preferenceResult.put("preferences", Collections.singletonMap("emailNotifications", true));

        Map<String, Object> notificationResult = new HashMap<>();
        notificationResult.put("sent", true);
        notificationResult.put("messageId", "msg-456");

        // Configure mocks
        when(userServiceClient.createUser(any())).thenReturn(userCreationResult);
        when(productCatalogClient.initializeUserPreferences(anyString(), anyMap())).thenReturn(preferenceResult);
        when(notificationServiceClient.sendNotification(anyMap())).thenReturn(notificationResult);

        // Execute the method under test
        Object result = moduleCoordinationService.coordinateUserRegistration(userData);

        // Verify the result
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be a Map", result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        assertEquals("User creation result should be included", userCreationResult, resultMap.get("user"));
        assertEquals("Preference initialization result should be included", preferenceResult, resultMap.get("preferences"));
        assertEquals("Notification result should be included", notificationResult, resultMap.get("notification"));

        // Verify interactions with mocked services
        verify(userServiceClient).createUser(userData);
        verify(productCatalogClient).initializeUserPreferences(eq("user-123"), anyMap());
        verify(notificationServiceClient).sendNotification(argThat(map -> 
            "user-123".equals(map.get("userId")) && 
            "WELCOME".equals(map.get("type")) &&
            "welcome_email".equals(map.get("template"))
        ));
    }

    /**
     * Tests order placement coordination functionality including:
     * 1. User validation
     * 2. Product availability check
     * 3. Order creation
     * 4. Order confirmation notification
     */
    @Test
    public void testCoordinateOrderPlacement() {
        // Prepare test data
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", "user-123");
        orderRequest.put("products", Arrays.asList(
            Collections.singletonMap("id", "prod-1"),
            Collections.singletonMap("id", "prod-2")
        ));
        orderRequest.put("shippingAddress", "123 Test St, Test City");

        Map<String, Object> orderCreationResult = new HashMap<>();
        orderCreationResult.put("orderId", "order-456");
        orderCreationResult.put("status", "CREATED");
        orderCreationResult.put("timestamp", System.currentTimeMillis());

        Map<String, Object> productValidationResult = new HashMap<>();
        productValidationResult.put("valid", true);
        productValidationResult.put("allAvailable", true);

        // Configure mocks
        when(userServiceClient.validateUser(anyString())).thenReturn(true);
        when(productCatalogClient.validateProductsAvailability(any())).thenReturn(productValidationResult);
        when(orderServiceClient.createOrder(any())).thenReturn(orderCreationResult);
        when(notificationServiceClient.sendNotification(anyMap())).thenReturn(Collections.singletonMap("sent", true));

        // Execute the method under test
        Object result = moduleCoordinationService.coordinateOrderPlacement(orderRequest);

        // Verify the result
        assertNotNull("Result should not be null", result);
        assertEquals("Order creation result should be returned", orderCreationResult, result);

        // Verify interactions with mocked services
        verify(userServiceClient).validateUser("user-123");
        verify(productCatalogClient).validateProductsAvailability(any());
        verify(orderServiceClient).createOrder(orderRequest);
        verify(notificationServiceClient).sendNotification(argThat(map -> 
            "user-123".equals(map.get("userId")) && 
            "order-456".equals(map.get("orderId")) &&
            "ORDER_CONFIRMATION".equals(map.get("type"))
        ));
    }

    /**
     * Tests aggregated data fetching functionality for user_orders type.
     * Verifies that data is correctly fetched from multiple services and combined.
     */
    @Test
    public void testFetchAggregatedData() {
        // Prepare test data
        String dataType = "user_orders";
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", "user-123");
        filters.put("startDate", "2023-01-01");
        filters.put("endDate", "2023-12-31");

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", "user-123");
        userDetails.put("name", "Test User");
        userDetails.put("email", "test@example.com");

        Map<String, Object> ordersList = new HashMap<>();
        ordersList.put("total", 2);
        ordersList.put("orders", Arrays.asList(
            Collections.singletonMap("orderId", "order-1"),
            Collections.singletonMap("orderId", "order-2")
        ));

        // Configure mocks
        when(userServiceClient.getUserDetails(anyString())).thenReturn(userDetails);
        when(orderServiceClient.getUserOrders(anyString(), anyMap())).thenReturn(ordersList);

        // Execute the method under test
        Object result = moduleCoordinationService.fetchAggregatedData(dataType, filters);

        // Verify the result
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be a Map", result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        assertEquals("User details should be included", userDetails, resultMap.get("user"));
        assertEquals("Orders list should be included", ordersList, resultMap.get("orders"));

        // Verify interactions with mocked services
        verify(userServiceClient).getUserDetails("user-123");
        verify(orderServiceClient).getUserOrders(eq("user-123"), eq(filters));
    }

    /**
     * Tests cross-service constraint validation functionality.
     * Verifies that constraints involving multiple services are correctly validated.
     */
    @Test
    public void testValidateCrossServiceConstraints() {
        // Prepare test data
        String operation = "place_order";
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user-123");
        data.put("products", Arrays.asList(
            Collections.singletonMap("id", "prod-1"),
            Collections.singletonMap("id", "prod-2")
        ));
        data.put("totalAmount", 150.0);

        // Configure mocks for success case
        when(userServiceClient.isUserActive(anyString())).thenReturn(true);
        when(productCatalogClient.checkProductsAvailability(any())).thenReturn(true);
        when(orderServiceClient.checkUserOrderLimits(anyString(), any())).thenReturn(true);

        // Test success case
        boolean result = moduleCoordinationService.validateCrossServiceConstraints(operation, data);
        assertTrue("Validation should succeed when all constraints are met", result);

        // Verify interactions
        verify(userServiceClient).isUserActive("user-123");
        verify(productCatalogClient).checkProductsAvailability(any());
        verify(orderServiceClient).checkUserOrderLimits(eq("user-123"), eq(data));

        // Reset mocks
        reset(userServiceClient, productCatalogClient, orderServiceClient);

        // Configure mocks for failure case - inactive user
        when(userServiceClient.isUserActive(anyString())).thenReturn(false);

        // Test failure case - inactive user
        result = moduleCoordinationService.validateCrossServiceConstraints(operation, data);
        assertFalse("Validation should fail when user is inactive", result);

        // Verify interactions - should stop after user validation fails
        verify(userServiceClient).isUserActive("user-123");
        verify(productCatalogClient, never()).checkProductsAvailability(any());
        verify(orderServiceClient, never()).checkUserOrderLimits(anyString(), any());
    }
}