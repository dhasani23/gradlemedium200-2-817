package com.gradlemedium200.userservice.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user entity in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    /**
     * Enum representing user roles in the system
     */
    public enum Role {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_MANAGER
    }
}