package com.gradlemedium200.userservice.repository;

import com.gradlemedium200.userservice.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for user data access operations with DynamoDB integration.
 * This repository handles basic CRUD operations for User entities as well as
 * custom query methods for user lookup by various attributes.
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user with the specified email exists.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user with the specified username exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    // TODO: Add methods for advanced querying with DynamoDB GSI if needed
    
    // FIXME: Consider adding pagination support for querying large datasets
}