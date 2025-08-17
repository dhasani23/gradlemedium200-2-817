package com.gradlemedium200.productcatalog.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.gradlemedium200.productcatalog.model.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository class for product data access operations using AWS DynamoDB.
 * 
 * This class provides methods for basic CRUD operations on Product entities
 * as well as additional query methods for common product search scenarios.
 */
@Repository
public class ProductRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Constructor for dependency injection
     * 
     * @param dynamoDBMapper AWS DynamoDB mapper for data operations
     */
    @Autowired
    public ProductRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Save or update a product in the database
     *
     * @param product the product to save or update
     * @return the saved product
     */
    public Product save(Product product) {
        logger.info("Saving product: {}", product.getProductId());
        
        // Set or update timestamps
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        
        // Save to DynamoDB
        dynamoDBMapper.save(product);
        logger.debug("Product saved successfully: {}", product.getProductId());
        
        return product;
    }

    /**
     * Find a product by its ID
     *
     * @param productId the ID of the product to find
     * @return an Optional containing the product if found, or empty if not found
     */
    public Optional<Product> findById(String productId) {
        logger.info("Finding product by ID: {}", productId);
        
        Product product = dynamoDBMapper.load(Product.class, productId);
        
        return Optional.ofNullable(product);
    }

    /**
     * Find all products in the database
     *
     * @return a list of all products
     */
    public List<Product> findAll() {
        logger.info("Finding all products");
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        
        // TODO: Implement pagination for large datasets to avoid performance issues
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }

    /**
     * Find products by category ID
     *
     * @param categoryId the category ID to search for
     * @return a list of products in the specified category
     */
    public List<Product> findByCategoryId(String categoryId) {
        logger.info("Finding products by category ID: {}", categoryId);
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":categoryId", new AttributeValue().withS(categoryId));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            .withFilterExpression("CategoryId = :categoryId")
            .withExpressionAttributeValues(eav);
            
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }

    /**
     * Delete a product by ID
     *
     * @param productId the ID of the product to delete
     */
    public void deleteById(String productId) {
        logger.info("Deleting product by ID: {}", productId);
        
        // Load the product first to ensure it exists
        Product product = dynamoDBMapper.load(Product.class, productId);
        
        if (product != null) {
            dynamoDBMapper.delete(product);
            logger.debug("Product deleted successfully: {}", productId);
        } else {
            logger.warn("Cannot delete product - not found: {}", productId);
        }
    }

    /**
     * Find products by status (ACTIVE, INACTIVE, DISCONTINUED)
     *
     * @param status the status to search for
     * @return a list of products with the specified status
     */
    public List<Product> findByStatus(String status) {
        logger.info("Finding products by status: {}", status);
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":status", new AttributeValue().withS(status));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            .withFilterExpression("Status = :status")
            .withExpressionAttributeValues(eav);
            
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }
    
    /**
     * Find products by brand name
     *
     * @param brand the brand to search for
     * @return a list of products with the specified brand
     */
    public List<Product> findByBrand(String brand) {
        logger.info("Finding products by brand: {}", brand);
        
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":brand", new AttributeValue().withS(brand));
        
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            .withFilterExpression("Brand = :brand")
            .withExpressionAttributeValues(eav);
            
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }
    
    /**
     * Check if a product exists by ID
     *
     * @param productId the ID to check
     * @return true if the product exists, false otherwise
     */
    public boolean existsById(String productId) {
        logger.debug("Checking if product exists: {}", productId);
        return findById(productId).isPresent();
    }

    // TODO: Implement batch operations for better performance with multiple products
    
    // TODO: Add support for advanced querying with multiple conditions
    
    // FIXME: Current implementation uses scan operations which are not efficient for large datasets.
    // Consider implementing Global Secondary Indexes for common query patterns.
}