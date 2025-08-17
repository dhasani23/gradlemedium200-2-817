package com.gradlemedium200.productcatalog.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.gradlemedium200.productcatalog.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repository for advanced search and filtering operations using DynamoDB scan operations.
 * This class provides methods to search products with various filter criteria.
 */
@Repository
public class SearchRepository {

    /**
     * DynamoDB mapper for search operations
     */
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Constructor for SearchRepository
     *
     * @param dynamoDBMapper DynamoDB mapper for database operations
     */
    @Autowired
    public SearchRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Search products with multiple filter criteria including text query, category, and price range.
     * This method builds a complex DynamoDB scan expression based on the provided filters.
     *
     * @param query      Text query to search in product name and description
     * @param categoryId Category ID to filter products by
     * @param minPrice   Minimum price for filtering
     * @param maxPrice   Maximum price for filtering
     * @return List of products matching the criteria
     */
    public List<Product> searchProducts(String query, String categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        List<String> filterExpressions = new ArrayList<>();

        // Add name/description search if query is provided
        if (StringUtils.hasText(query)) {
            expressionAttributeValues.put(":queryValue", new AttributeValue().withS(query.toLowerCase()));
            filterExpressions.add("contains(lower(#name), :queryValue) OR contains(lower(#description), :queryValue)");
        }

        // Add category filter if provided
        if (StringUtils.hasText(categoryId)) {
            expressionAttributeValues.put(":categoryId", new AttributeValue().withS(categoryId));
            filterExpressions.add("#categoryId = :categoryId");
        }

        // Add price range filter if provided
        if (minPrice != null) {
            expressionAttributeValues.put(":minPrice", new AttributeValue().withN(minPrice.toString()));
            filterExpressions.add("#price >= :minPrice");
        }

        if (maxPrice != null) {
            expressionAttributeValues.put(":maxPrice", new AttributeValue().withN(maxPrice.toString()));
            filterExpressions.add("#price <= :maxPrice");
        }

        // Add filter for active products only
        expressionAttributeValues.put(":activeStatus", new AttributeValue().withS("ACTIVE"));
        filterExpressions.add("#status = :activeStatus");

        // Create scan expression
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        // Set expression attribute names
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#name", "Name");
        expressionAttributeNames.put("#description", "Description");
        expressionAttributeNames.put("#categoryId", "CategoryId");
        expressionAttributeNames.put("#price", "Price");
        expressionAttributeNames.put("#status", "Status");
        scanExpression.setExpressionAttributeNames(expressionAttributeNames);

        // Combine all filter expressions with AND operator
        if (!filterExpressions.isEmpty()) {
            scanExpression.setFilterExpression(String.join(" AND ", filterExpressions));
            scanExpression.setExpressionAttributeValues(expressionAttributeValues);
        }

        // Execute the scan operation
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }

    /**
     * Search products by tags. This method finds products that have any of the specified tags.
     *
     * @param tags Set of tags to search for
     * @return List of products that have any of the specified tags
     */
    public List<Product> searchByTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        // Create a filter condition for each tag
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        List<String> filterExpressions = new ArrayList<>();

        int tagIndex = 0;
        for (String tag : tags) {
            String tagKey = ":tag" + tagIndex;
            expressionAttributeValues.put(tagKey, new AttributeValue().withS(tag));
            filterExpressions.add("contains(#tags, " + tagKey + ")");
            tagIndex++;
        }

        // Add filter for active products only
        expressionAttributeValues.put(":activeStatus", new AttributeValue().withS("ACTIVE"));
        filterExpressions.add("#status = :activeStatus");

        // Create scan expression
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        
        // Set expression attribute names
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#tags", "Tags");
        expressionAttributeNames.put("#status", "Status");
        scanExpression.setExpressionAttributeNames(expressionAttributeNames);

        // Combine tag expressions with OR operator, and status with AND
        String tagExpression = "(" + String.join(" OR ", filterExpressions.subList(0, filterExpressions.size() - 1)) + ")";
        String finalExpression = tagExpression + " AND " + filterExpressions.get(filterExpressions.size() - 1);
        
        scanExpression.setFilterExpression(finalExpression);
        scanExpression.setExpressionAttributeValues(expressionAttributeValues);

        // Execute the scan operation
        return dynamoDBMapper.scan(Product.class, scanExpression);
    }

    /**
     * Find products similar to a given product based on category and tags.
     * Similarity is determined by shared category and matching tags.
     *
     * @param productId ID of the product to find similar products for
     * @param limit     Maximum number of similar products to return
     * @return List of similar products
     */
    public List<Product> findSimilarProducts(String productId, int limit) {
        // Get the reference product
        Product referenceProduct = dynamoDBMapper.load(Product.class, productId);
        
        if (referenceProduct == null) {
            return new ArrayList<>();
        }

        // Create expression to find products in the same category
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":categoryId", new AttributeValue().withS(referenceProduct.getCategoryId()));
        expressionAttributeValues.put(":productId", new AttributeValue().withS(productId));
        expressionAttributeValues.put(":activeStatus", new AttributeValue().withS("ACTIVE"));
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#categoryId", "CategoryId");
        expressionAttributeNames.put("#productId", "ProductId");
        expressionAttributeNames.put("#status", "Status");
        
        scanExpression.setFilterExpression("#categoryId = :categoryId AND #productId <> :productId AND #status = :activeStatus");
        scanExpression.setExpressionAttributeValues(expressionAttributeValues);
        scanExpression.setExpressionAttributeNames(expressionAttributeNames);
        
        // Scan for all products in the same category
        List<Product> sameCategory = dynamoDBMapper.scan(Product.class, scanExpression);
        
        // Sort by similarity (number of matching tags)
        List<Product> sortedBySimilarity = sameCategory.stream()
                .filter(p -> p.getTags() != null && referenceProduct.getTags() != null)
                .sorted((p1, p2) -> {
                    // Count matching tags for each product
                    long p1Matches = p1.getTags().stream()
                            .filter(tag -> referenceProduct.getTags().contains(tag))
                            .count();
                    long p2Matches = p2.getTags().stream()
                            .filter(tag -> referenceProduct.getTags().contains(tag))
                            .count();
                    
                    // Sort descending by number of matches
                    return Long.compare(p2Matches, p1Matches);
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        return sortedBySimilarity;
    }

    /**
     * Find trending products based on recent activity and popularity metrics.
     * This is a simplified implementation that could be enhanced with real activity data.
     *
     * @param limit Maximum number of trending products to return
     * @return List of trending products
     */
    public List<Product> findTrendingProducts(int limit) {
        // In a real implementation, this would use metrics like:
        // - Recent view counts
        // - Purchase frequency
        // - Rating trends
        // - Time-weighted popularity
        
        // For now, we'll implement a simplified version that returns recently added products
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        
        // Only return active products
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":activeStatus", new AttributeValue().withS("ACTIVE"));
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#status", "Status");
        
        scanExpression.setFilterExpression("#status = :activeStatus");
        scanExpression.setExpressionAttributeValues(expressionAttributeValues);
        scanExpression.setExpressionAttributeNames(expressionAttributeNames);
        
        // Scan for active products
        List<Product> activeProducts = dynamoDBMapper.scan(Product.class, scanExpression);
        
        // Sort by creation date (most recent first) and limit results
        List<Product> trendingProducts = activeProducts.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
        
        return trendingProducts;
        
        // TODO: Implement a more sophisticated trending algorithm using view counts and purchase data
        // FIXME: This implementation doesn't account for actual product popularity
    }
}