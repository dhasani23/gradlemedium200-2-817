package com.gradlemedium200.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with the User Service.
 */
@Component
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    /**
     * Constructor.
     * @param restTemplate the REST template
     * @param baseUrl the base URL of the User Service
     */
    public UserServiceClient(
            RestTemplate restTemplate,
            @Value("${service.user.url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    /**
     * Get user information by ID.
     * @param userId the user ID
     * @return the user information
     */
    public Object getUserInfo(String userId) {
        String url = baseUrl + "/users/" + userId;
        return restTemplate.getForObject(url, Object.class);
    }
    
    /**
     * Check if a user exists.
     * @param userId the user ID
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/exists";
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error checking if user exists", e);
            return false;
        }
    }
    
    /**
     * Alias for userExists method to maintain compatibility with ModuleCoordinationService
     * @param userId the user ID
     * @return true if the user exists, false otherwise
     */
    public boolean doesUserExist(String userId) {
        return userExists(userId);
    }
    
    /**
     * Authenticate a user.
     * @param username the username
     * @param password the password
     * @return an authentication token if successful, null otherwise
     */
    public String authenticateUser(String username, String password) {
        try {
            String url = baseUrl + "/auth/login";
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            return response != null ? (String) response.get("token") : null;
        } catch (Exception e) {
            logger.error("Error authenticating user", e);
            return null;
        }
    }
    
    /**
     * Get user preferences.
     * @param userId the user ID
     * @return the user preferences
     */
    public Object getUserPreferences(String userId) {
        String url = baseUrl + "/users/" + userId + "/preferences";
        return restTemplate.getForObject(url, Object.class);
    }
    
    /**
     * Update user preferences.
     * @param userId the user ID
     * @param preferences the preferences to update
     * @return true if the preferences were updated successfully, false otherwise
     */
    public boolean updateUserPreferences(String userId, Object preferences) {
        try {
            String url = baseUrl + "/users/" + userId + "/preferences";
            restTemplate.put(url, preferences);
            return true;
        } catch (Exception e) {
            logger.error("Error updating user preferences", e);
            return false;
        }
    }
    
    /**
     * Create a new user.
     * @param userData the user data
     * @return the created user
     */
    public Object createUser(Object userData) {
        try {
            String url = baseUrl + "/users";
            return restTemplate.postForObject(url, userData, Object.class);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Validate a user.
     * @param userId the user ID
     * @return true if the user is valid, false otherwise
     */
    public boolean validateUser(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/validate";
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error validating user", e);
            return false;
        }
    }
    
    /**
     * Get user details.
     * @param userId the user ID
     * @return the user details
     */
    public Object getUserDetails(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/details";
            return restTemplate.getForObject(url, Object.class);
        } catch (Exception e) {
            logger.error("Error getting user details", e);
            return null;
        }
    }
    
    /**
     * Check if a user is active.
     * @param userId the user ID
     * @return true if the user is active, false otherwise
     */
    public boolean isUserActive(String userId) {
        try {
            String url = baseUrl + "/users/" + userId + "/status";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && "ACTIVE".equals(response.get("status"));
        } catch (Exception e) {
            logger.error("Error checking if user is active", e);
            return false;
        }
    }
    
    /**
     * Check if a user has opted in for notifications of a specific type.
     * @param userId the user ID
     * @param notificationType the notification type
     * @return true if the user has opted in, false otherwise
     */
    public boolean hasUserOptedInForNotifications(String userId, String notificationType) {
        try {
            String url = baseUrl + "/users/" + userId + "/notifications/opted-in/" + notificationType;
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Error checking if user opted in for notifications", e);
            return false;
        }
    }
}