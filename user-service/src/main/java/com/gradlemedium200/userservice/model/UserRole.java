package com.gradlemedium200.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a user role in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    
    private String id;
    private String userId;
    private User.Role role;
    private String createdBy;
    private Long createdAt;
    
    /**
     * Create a UserRole with the given role.
     * 
     * @param userId The user ID
     * @param role The role to assign
     */
    public UserRole(String userId, User.Role role) {
        this.userId = userId;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }
    
    /**
     * Create a UserRole with the given role and creator.
     * 
     * @param userId The user ID
     * @param role The role to assign
     * @param createdBy The ID of the user who created this role
     */
    public UserRole(String userId, User.Role role, String createdBy) {
        this.userId = userId;
        this.role = role;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
    }
}