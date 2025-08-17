package com.gradlemedium200.userservice.service;

import com.gradlemedium200.userservice.model.User;
import com.gradlemedium200.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for token management and authentication.
 */
@Service
public class TokenService {

    private final JwtUtil jwtUtil;
    
    // Simple in-memory token store - in a production environment, this would be in Redis or a similar store
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    @Autowired
    public TokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generate a new token for a user.
     *
     * @param user The user to generate token for
     * @return The generated JWT token
     */
    public String generateToken(User user) {
        String token = jwtUtil.generateToken(user);
        activeTokens.put(user.getUsername(), token);
        return token;
    }

    /**
     * Validate a token.
     *
     * @param token The token to validate
     * @param username The username to validate against
     * @return True if the token is valid, false otherwise
     */
    public boolean validateToken(String token, String username) {
        // Check if token is in our active tokens store
        String storedToken = activeTokens.get(username);
        if (storedToken == null || !storedToken.equals(token)) {
            return false;
        }
        
        return jwtUtil.validateToken(token, username);
    }

    /**
     * Invalidate a user's token (used for logout).
     *
     * @param username The username whose token to invalidate
     */
    public void invalidateToken(String username) {
        activeTokens.remove(username);
    }

    /**
     * Get user ID from token.
     *
     * @param token The JWT token
     * @return The user ID
     */
    public String getUserIdFromToken(String token) {
        return jwtUtil.extractClaim(token, claims -> claims.get("userId", String.class));
    }
}