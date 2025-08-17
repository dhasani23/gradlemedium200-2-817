package com.gradlemedium200.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for authentication and authorization across the platform.
 * 
 * This class configures Spring Security with JWT authentication, defines security rules
 * for different endpoints, and sets up necessary security beans like password encoder
 * and authentication manager.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * JWT secret key for token validation.
     * Should be moved to a secure vault in production.
     */
    @Value("${security.jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds.
     * Default is 24 hours.
     */
    @Value("${security.jwt.expiration:86400000}")
    private long tokenExpiration;

    /**
     * Creates a BCrypt password encoder bean for password hashing.
     * 
     * @return PasswordEncoder instance with BCrypt implementation
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Using BCrypt with default strength (10)
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates an authentication manager bean.
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if authentication manager creation fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Get the authentication manager from the configuration
        return config.getAuthenticationManager();
    }

    /**
     * Configures HTTP security filter chain with authorization rules.
     * 
     * @param http HttpSecurity to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure security rules
        http
            // Disable CSRF for stateless API
            .csrf().disable()
            // Set session management to stateless
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            // Configure authorization rules
            .authorizeRequests()
                // Public endpoints that don't require authentication
                .antMatchers("/api/auth/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Admin only endpoints
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // User endpoints require authentication
                .antMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                // Product catalog endpoints are public
                .antMatchers("/api/products/**").permitAll()
                // All other requests need authentication
                .anyRequest().authenticated();
        
        // TODO: Add JWT token filter once JWT implementation is completed
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // FIXME: Consider adding CORS configuration for production deployment
        
        return http.build();
    }
}