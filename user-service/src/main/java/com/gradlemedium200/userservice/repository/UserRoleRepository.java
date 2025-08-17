package com.gradlemedium200.userservice.repository;

import com.gradlemedium200.userservice.model.User;
import com.gradlemedium200.userservice.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing UserRole entities.
 */
public interface UserRoleRepository {

    /**
     * Find all roles for a specific user.
     *
     * @param userId The user ID
     * @return List of UserRole objects for the user
     */
    List<UserRole> findByUserId(String userId);

    /**
     * Find a specific role for a user.
     *
     * @param userId The user ID
     * @param role The role to find
     * @return Optional containing the UserRole if found
     */
    Optional<UserRole> findByUserIdAndRole(String userId, User.Role role);

    /**
     * Save a user role.
     *
     * @param userRole The user role to save
     * @return The saved user role
     */
    UserRole save(UserRole userRole);

    /**
     * Delete a user role.
     *
     * @param userRole The user role to delete
     */
    void delete(UserRole userRole);

    /**
     * Delete all roles for a user.
     *
     * @param userId The user ID
     */
    void deleteByUserId(String userId);

    /**
     * Find all users with a specific role.
     *
     * @param role The role to search for
     * @return List of UserRole objects with the specified role
     */
    List<UserRole> findByRole(User.Role role);
}