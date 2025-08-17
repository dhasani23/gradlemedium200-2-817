package com.gradlemedium200.notification.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gradlemedium200.notification.model.NotificationTemplate;
import com.gradlemedium200.notification.model.NotificationType;

/**
 * Repository interface for notification template data access operations 
 * including template management and retrieval.
 * <p>
 * This interface provides methods to store, retrieve, update, and delete 
 * notification templates. It supports operations for finding templates by
 * various criteria such as ID, notification type, and language, as well as
 * retrieving active templates.
 * </p>
 * <p>
 * Implementations of this interface may use various data storage technologies
 * such as relational databases or NoSQL document stores.
 * </p>
 * 
 * @author gradlemedium200
 */
public interface TemplateRepository {
    
    /**
     * Saves a notification template.
     * <p>
     * If the template doesn't exist, it will be created.
     * If it already exists, it will be updated.
     * </p>
     * 
     * @param template The notification template to save
     * @return The saved notification template with any updated metadata
     * @throws IllegalArgumentException if template is null
     */
    NotificationTemplate save(NotificationTemplate template);
    
    /**
     * Finds a template by its unique identifier.
     * 
     * @param id The template ID
     * @return An Optional containing the found template, or empty if not found
     * @throws IllegalArgumentException if id is null
     */
    Optional<NotificationTemplate> findById(String id);
    
    /**
     * Finds all templates for a specific notification type.
     * <p>
     * This method retrieves all templates associated with the given notification
     * type regardless of their language or active status.
     * </p>
     * 
     * @param type The notification type to filter by
     * @return A list of templates for the specified type, or an empty list if none found
     * @throws IllegalArgumentException if type is null
     */
    List<NotificationTemplate> findByType(NotificationType type);
    
    /**
     * Finds all templates for a specific notification type.
     * <p>
     * This method retrieves all templates associated with the given notification
     * type regardless of their language or active status.
     * </p>
     * 
     * @param typeStr The notification type as a string
     * @return A list of templates for the specified type, or an empty list if none found
     * @throws IllegalArgumentException if type is null
     */
    default List<NotificationTemplate> findByType(String typeStr) {
        try {
            NotificationType type = NotificationType.valueOf(typeStr);
            return findByType(type);
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Finds a template by type and language.
     * <p>
     * This method is useful for retrieving localized templates for a specific
     * notification type. If multiple templates match the criteria, the implementation
     * should return the most appropriate one based on template metadata.
     * </p>
     * 
     * @param type The notification type
     * @param language The language code (e.g., "en", "es", "fr")
     * @return An Optional containing the found template, or empty if not found
     * @throws IllegalArgumentException if type or language is null
     */
    Optional<NotificationTemplate> findByTypeAndLanguage(NotificationType type, String language);
    
    /**
     * Finds a template by type string and language.
     * <p>
     * This method is useful for retrieving localized templates for a specific
     * notification type. If multiple templates match the criteria, the implementation
     * should return the most appropriate one based on template metadata.
     * </p>
     * 
     * @param type The notification type as string
     * @param language The language code (e.g., "en", "es", "fr")
     * @return An Optional containing the found template, or empty if not found
     * @throws IllegalArgumentException if type or language is null
     */
    default Optional<NotificationTemplate> findByTypeAndLanguage(String type, String language) {
        try {
            NotificationType typEnum = NotificationType.valueOf(type);
            return findByTypeAndLanguage(typEnum, language);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Finds all active templates.
     * <p>
     * Active templates are those that have their isActive flag set to true.
     * </p>
     * 
     * @return A list of active templates, or an empty list if none found
     */
    List<NotificationTemplate> findActiveTemplates();
    
    /**
     * Finds all templates in the system.
     * <p>
     * This method retrieves all templates regardless of type, language, or active status.
     * </p>
     * 
     * @return A list of all templates, or an empty list if none found
     */
    List<NotificationTemplate> findAllTemplates();
    
    /**
     * Updates an existing template.
     * <p>
     * This method updates all template properties with the provided template object.
     * The template must already exist in the repository.
     * </p>
     * 
     * @param template The template with updated values
     * @return The updated template with any metadata changes
     * @throws IllegalArgumentException if template is null
     * @throws IllegalStateException if the template doesn't exist in the repository
     */
    NotificationTemplate updateTemplate(NotificationTemplate template);
    
    /**
     * Deletes a template by its unique identifier.
     * 
     * @param id The ID of the template to delete
     * @throws IllegalArgumentException if id is null
     * @throws IllegalStateException if the template doesn't exist
     */
    void deleteById(String id);
    
    // TODO: Add method for finding templates by creation date range
    
    // TODO: Add method for bulk template operations to improve performance
    
    // FIXME: Consider adding version control for templates to track changes
}