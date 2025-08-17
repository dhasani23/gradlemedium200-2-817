package com.gradlemedium200.productcatalog.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.productcatalog.model.Category;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository interface for category data access operations using DynamoDB.
 * Provides methods for CRUD operations on Category entities and specialized
 * queries for category hierarchy management.
 *
 * @author gradlemedium200
 */
public class CategoryRepository {
    
    /**
     * DynamoDB mapper for data operations
     */
    private final DynamoDBMapper dynamoDBMapper;
    
    /**
     * Constructor for dependency injection of the DynamoDB mapper
     *
     * @param dynamoDBMapper the DynamoDB mapper instance
     */
    public CategoryRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }
    
    /**
     * Save or update a category in DynamoDB
     *
     * @param category the category to save or update
     * @return the saved category with any generated properties
     */
    public Category save(Category category) {
        // Update timestamp before saving
        category.updateTimestamp();
        dynamoDBMapper.save(category);
        return category;
    }
    
    /**
     * Find a category by its unique ID
     *
     * @param categoryId the ID of the category to find
     * @return an Optional containing the category if found, or empty if not found
     */
    public Optional<Category> findById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Category category = dynamoDBMapper.load(Category.class, categoryId);
        return Optional.ofNullable(category);
    }
    
    /**
     * Find all categories in the database
     *
     * @return a list of all categories
     */
    public List<Category> findAll() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return dynamoDBMapper.scan(Category.class, scanExpression);
    }
    
    /**
     * Find all subcategories that belong to a specific parent category
     *
     * @param parentCategoryId the ID of the parent category
     * @return a list of subcategories belonging to the specified parent
     */
    public List<Category> findByParentCategoryId(String parentCategoryId) {
        if (parentCategoryId == null || parentCategoryId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":parentId", new AttributeValue().withS(parentCategoryId));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("parentCategoryId = :parentId")
                .withExpressionAttributeValues(eav);
        
        return dynamoDBMapper.scan(Category.class, scanExpression);
    }
    
    /**
     * Find all root-level categories (categories without a parent)
     *
     * @return a list of root categories
     */
    public List<Category> findRootCategories() {
        // Query for categories with level=0 or null parentCategoryId
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Category> allCategories = dynamoDBMapper.scan(Category.class, scanExpression);
        
        // Filter to find root categories
        return allCategories.stream()
                .filter(Category::isRootCategory)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a category by its ID
     *
     * @param categoryId the ID of the category to delete
     */
    public void deleteById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return;
        }
        
        // First check if category exists
        Optional<Category> categoryOptional = findById(categoryId);
        
        categoryOptional.ifPresent(category -> {
            // TODO: Consider checking for child categories before deletion
            // and implement a strategy (cascade delete or prevent deletion)
            dynamoDBMapper.delete(category);
        });
    }
    
    /**
     * Find active categories only
     * 
     * @return list of active categories
     */
    public List<Category> findActiveCategories() {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":isActiveVal", new AttributeValue().withBOOL(true));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("isActive = :isActiveVal")
                .withExpressionAttributeValues(eav);
        
        return dynamoDBMapper.scan(Category.class, scanExpression);
    }
    
    /**
     * Find categories by level in the hierarchy
     *
     * @param level the hierarchy level (0 for root)
     * @return list of categories at the specified level
     */
    public List<Category> findByLevel(int level) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":levelVal", new AttributeValue().withN(String.valueOf(level)));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("level = :levelVal")
                .withExpressionAttributeValues(eav);
        
        return dynamoDBMapper.scan(Category.class, scanExpression);
    }
    
    // FIXME: Implement a more efficient query mechanism using GSI instead of scans
    // TODO: Add batch get/delete operations for better performance
    // TODO: Implement pagination for queries returning large result sets
}