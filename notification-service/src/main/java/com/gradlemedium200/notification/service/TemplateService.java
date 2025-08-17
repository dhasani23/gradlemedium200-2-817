package com.gradlemedium200.notification.service;

import com.gradlemedium200.notification.model.NotificationTemplate;
import com.gradlemedium200.notification.model.NotificationType;
import com.gradlemedium200.notification.repository.TemplateRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing notification templates, template processing, and dynamic content generation
 * with placeholder replacement and internationalization support.
 * <p>
 * This service provides functionality for creating, retrieving, updating, and deleting notification
 * templates. It also handles template processing including placeholder extraction and replacement,
 * as well as template validation and caching to improve performance.
 * </p>
 *
 * @author gradlemedium200
 */
@Service
public class TemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
    
    private final TemplateRepository templateRepository;
    private final String defaultLanguage;
    private final Map<String, NotificationTemplate> templateCache;
    
    /**
     * Creates a new TemplateService with the specified repository and default language.
     *
     * @param templateRepository Repository for managing notification templates
     * @param defaultLanguage Default language for templates, injected from configuration
     */
    @Autowired
    public TemplateService(
            TemplateRepository templateRepository,
            @Value("${notification.template.defaultLanguage:en}") String defaultLanguage) {
        this.templateRepository = templateRepository;
        this.defaultLanguage = defaultLanguage;
        this.templateCache = new ConcurrentHashMap<>();
        
        logger.info("Template service initialized with default language: {}", defaultLanguage);
    }
    
    /**
     * Initializes the template cache with frequently used templates.
     */
    @PostConstruct
    public void initializeCache() {
        logger.info("Initializing template cache with active templates");
        try {
            List<NotificationTemplate> activeTemplates = templateRepository.findActiveTemplates();
            for (NotificationTemplate template : activeTemplates) {
                String cacheKey = generateCacheKey(template.getType(), template.getLanguage());
                templateCache.put(cacheKey, template);
            }
            logger.info("Template cache initialized with {} templates", activeTemplates.size());
        } catch (Exception e) {
            logger.error("Failed to initialize template cache", e);
        }
    }
    
    /**
     * Gets a template by type and language. If no template is found for the specified language,
     * falls back to the default language.
     *
     * @param type The notification type
     * @param language The language code (e.g., "en", "es", "fr")
     * @return An Optional containing the found template, or empty if not found
     * @throws IllegalArgumentException if type is null
     */
    public Optional<NotificationTemplate> getTemplate(NotificationType type, String language) {
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        
        String requestedLanguage = StringUtils.hasText(language) ? language : defaultLanguage;
        String typeStr = type.name();
        
        // Try to get from cache first
        String cacheKey = generateCacheKey(typeStr, requestedLanguage);
        NotificationTemplate cachedTemplate = templateCache.get(cacheKey);
        if (cachedTemplate != null) {
            logger.debug("Template cache hit for type: {} and language: {}", type, requestedLanguage);
            return Optional.of(cachedTemplate);
        }
        
        // If not in cache, get from repository
        logger.debug("Template cache miss for type: {} and language: {}", type, requestedLanguage);
        Optional<NotificationTemplate> template = templateRepository.findByTypeAndLanguage(type, requestedLanguage);
        
        // If not found and requested language is not the default, try with default language
        if (!template.isPresent() && !requestedLanguage.equals(defaultLanguage)) {
            logger.debug("Template not found for language: {}, trying default language: {}", 
                    requestedLanguage, defaultLanguage);
            template = templateRepository.findByTypeAndLanguage(type, defaultLanguage);
        }
        
        // Cache the template if found
        template.ifPresent(t -> {
            templateCache.put(cacheKey, t);
            logger.debug("Template added to cache: {}", cacheKey);
        });
        
        return template;
    }
    
    /**
     * Processes template by replacing placeholders with values from the provided variables map.
     *
     * @param template The template string to process
     * @param variables Map of variable names to their values
     * @return The processed template content with placeholders replaced
     * @throws IllegalArgumentException if template or variables is null
     */
    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null) {
            throw new IllegalArgumentException("Template string cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        return replacePlaceholders(template, variables);
    }
    
    /**
     * Processes template by replacing placeholders with values from the provided variables map.
     *
     * @param template The notification template to process
     * @param variables Map of variable names to their values
     * @return The processed template content with placeholders replaced
     * @throws IllegalArgumentException if template or variables is null
     */
    public String processTemplate(NotificationTemplate template, Map<String, String> variables) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        logger.debug("Processing template: {}", template.getId());
        
        // Process subject and body
        String processedSubject = replacePlaceholders(template.getSubject(), variables);
        String processedBody = replacePlaceholders(template.getBody(), variables);
        
        // For HTML templates, process HTML body if available
        String processedHtmlBody = null;
        if (StringUtils.hasText(template.getHtmlBody())) {
            processedHtmlBody = replacePlaceholders(template.getHtmlBody(), variables);
        }
        
        // For now, return the processed body
        // TODO: Consider returning a structured object with subject, body, and htmlBody
        return processedBody;
    }
    
    /**
     * Creates a new notification template.
     *
     * @param template The template to create
     * @return The created template with its ID and metadata
     * @throws IllegalArgumentException if template is null or invalid
     */
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        
        // Validate the template
        if (!validateTemplate(template)) {
            throw new IllegalArgumentException("Template validation failed");
        }
        
        // Extract and set placeholders
        Set<String> placeholders = extractPlaceholders(template.getBody());
        if (StringUtils.hasText(template.getSubject())) {
            placeholders.addAll(extractPlaceholders(template.getSubject()));
        }
        if (StringUtils.hasText(template.getHtmlBody())) {
            placeholders.addAll(extractPlaceholders(template.getHtmlBody()));
        }
        template.setPlaceholders(placeholders);
        
        // Save the template
        NotificationTemplate savedTemplate = templateRepository.save(template);
        
        // Add to cache if active
        if (savedTemplate.isActive()) {
            String cacheKey = generateCacheKey(savedTemplate.getType(), savedTemplate.getLanguage());
            templateCache.put(cacheKey, savedTemplate);
            logger.debug("New template added to cache: {}", cacheKey);
        }
        
        logger.info("Created new template with ID: {}", savedTemplate.getId());
        return savedTemplate;
    }
    
    /**
     * Updates an existing notification template.
     *
     * @param template The template with updated values
     * @return The updated template
     * @throws IllegalArgumentException if template is null or invalid
     * @throws IllegalStateException if the template doesn't exist
     */
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        if (template.getId() == null) {
            throw new IllegalArgumentException("Template ID cannot be null for updates");
        }
        
        // Validate the template
        if (!validateTemplate(template)) {
            throw new IllegalArgumentException("Template validation failed");
        }
        
        // Check if template exists
        if (!templateRepository.findById(template.getId()).isPresent()) {
            throw new IllegalStateException("Template not found with ID: " + template.getId());
        }
        
        // Extract and set placeholders
        Set<String> placeholders = extractPlaceholders(template.getBody());
        if (StringUtils.hasText(template.getSubject())) {
            placeholders.addAll(extractPlaceholders(template.getSubject()));
        }
        if (StringUtils.hasText(template.getHtmlBody())) {
            placeholders.addAll(extractPlaceholders(template.getHtmlBody()));
        }
        template.setPlaceholders(placeholders);
        
        // Update the template
        NotificationTemplate updatedTemplate = templateRepository.updateTemplate(template);
        
        // Update cache if active, remove from cache if not active
        String cacheKey = generateCacheKey(updatedTemplate.getType(), updatedTemplate.getLanguage());
        if (updatedTemplate.isActive()) {
            templateCache.put(cacheKey, updatedTemplate);
            logger.debug("Updated template in cache: {}", cacheKey);
        } else {
            templateCache.remove(cacheKey);
            logger.debug("Removed inactive template from cache: {}", cacheKey);
        }
        
        logger.info("Updated template with ID: {}", updatedTemplate.getId());
        return updatedTemplate;
    }
    
    /**
     * Deletes a notification template.
     *
     * @param templateId The ID of the template to delete
     * @throws IllegalArgumentException if templateId is null
     * @throws IllegalStateException if the template doesn't exist
     */
    public void deleteTemplate(String templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID cannot be null");
        }
        
        // Get the template to remove from cache after deletion
        Optional<NotificationTemplate> templateToDelete = templateRepository.findById(templateId);
        
        // Delete the template
        templateRepository.deleteById(templateId);
        
        // Remove from cache if it was present
        templateToDelete.ifPresent(template -> {
            String cacheKey = generateCacheKey(template.getType(), template.getLanguage());
            templateCache.remove(cacheKey);
            logger.debug("Removed deleted template from cache: {}", cacheKey);
        });
        
        logger.info("Deleted template with ID: {}", templateId);
    }
    
    /**
     * Validates template structure and placeholders.
     *
     * @param template The template to validate
     * @return true if the template is valid, false otherwise
     */
    public boolean validateTemplate(NotificationTemplate template) {
        if (template == null) {
            logger.warn("Validation failed: Template is null");
            return false;
        }
        
        if (template.getType() == null) {
            logger.warn("Validation failed: Template type is null");
            return false;
        }
        
        if (!StringUtils.hasText(template.getBody())) {
            logger.warn("Validation failed: Template body is empty");
            return false;
        }
        
        if (!StringUtils.hasText(template.getLanguage())) {
            logger.warn("Validation failed: Template language is not specified");
            return false;
        }
        
        // Check for unbalanced placeholders
        boolean subjectValid = true;
        boolean bodyValid = true;
        boolean htmlBodyValid = true;
        
        if (StringUtils.hasText(template.getSubject())) {
            subjectValid = validatePlaceholderSyntax(template.getSubject());
        }
        
        bodyValid = validatePlaceholderSyntax(template.getBody());
        
        if (StringUtils.hasText(template.getHtmlBody())) {
            htmlBodyValid = validatePlaceholderSyntax(template.getHtmlBody());
        }
        
        boolean isValid = subjectValid && bodyValid && htmlBodyValid;
        
        if (!isValid) {
            logger.warn("Template validation failed due to invalid placeholder syntax");
        }
        
        return isValid;
    }
    
    /**
     * Gets all templates for a specific notification type.
     *
     * @param type The notification type
     * @return A list of templates for the specified type, or an empty list if none found
     * @throws IllegalArgumentException if type is null
     */
    public List<NotificationTemplate> getTemplatesByType(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        
        return templateRepository.findByType(type);
    }
    
    /**
     * Gets all templates available in the system.
     *
     * @return A list of all available templates
     */
    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAllTemplates();
    }
    
    /**
     * Extracts placeholder variables from template text.
     * <p>
     * Placeholders are expected to be in the format {{variableName}}.
     * </p>
     *
     * @param templateText The template text to extract placeholders from
     * @return A set of placeholder variable names (without the {{ and }} delimiters)
     * @throws IllegalArgumentException if templateText is null
     */
    public Set<String> extractPlaceholders(String templateText) {
        if (templateText == null) {
            throw new IllegalArgumentException("Template text cannot be null");
        }
        
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            placeholders.add(matcher.group(1).trim());
        }
        
        logger.debug("Extracted {} placeholders from template text", placeholders.size());
        return placeholders;
    }
    
    /**
     * Replaces placeholders in text with actual values.
     * <p>
     * Placeholders are expected to be in the format {{variableName}}.
     * </p>
     *
     * @param text The text containing placeholders
     * @param variables Map of variable names to their values
     * @return The text with placeholders replaced by their values
     * @throws IllegalArgumentException if text or variables is null
     */
    public String replacePlaceholders(String text, Map<String, String> variables) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            String replacement = variables.getOrDefault(placeholder, "");
            
            // Escape $ and \ in the replacement string to avoid issues with Matcher.appendReplacement
            replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Clears the template cache.
     */
    public void clearCache() {
        logger.info("Clearing template cache");
        templateCache.clear();
    }
    
    /**
     * Generates a cache key for a template based on type and language.
     *
     * @param type The notification type
     * @param language The language code
     * @return The cache key string
     */
    private String generateCacheKey(NotificationType type, String language) {
        return type.name() + "_" + language;
    }
    
    /**
     * Generates a cache key for a template based on type and language.
     *
     * @param type The notification type as a string
     * @param language The language code
     * @return The cache key string
     */
    private String generateCacheKey(String type, String language) {
        return type + "_" + language;
    }
    
    /**
     * Validates that placeholders have proper syntax (balanced {{ and }}).
     *
     * @param text The text to validate
     * @return true if placeholder syntax is valid, false otherwise
     */
    private boolean validatePlaceholderSyntax(String text) {
        if (text == null) {
            return false;
        }
        
        int openCount = 0;
        int closeCount = 0;
        boolean openSequence = false;
        
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '{' && text.charAt(i + 1) == '{') {
                openCount++;
                openSequence = true;
                i++; // Skip the next '{' since we already counted it
            } else if (text.charAt(i) == '}' && text.charAt(i + 1) == '}') {
                closeCount++;
                if (!openSequence) {
                    // Found closing sequence without matching opening sequence
                    return false;
                }
                openSequence = false;
                i++; // Skip the next '}' since we already counted it
            }
        }
        
        return openCount == closeCount;
    }
    
    // TODO: Add support for template versioning to track changes over time
    
    // TODO: Implement bulk template processing for efficiency with large batches
    
    // FIXME: Improve cache eviction strategy to prevent memory issues with too many templates
}