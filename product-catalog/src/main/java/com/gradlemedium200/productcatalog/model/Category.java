package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity model representing a product category with hierarchical structure.
 * Categories can be organized in a parent-child relationship forming a tree structure.
 * 
 * @author gradlemedium200
 */
@DynamoDBTable(tableName = "ProductCategories")
public class Category {

    private String categoryId;
    private String name;
    private String description;
    private String parentCategoryId;
    private int level;
    private int sortOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by DynamoDB mapper.
     */
    public Category() {
        // Required empty constructor for DynamoDB
    }

    /**
     * Constructor for creating a new category with essential fields.
     * 
     * @param categoryId unique identifier for the category
     * @param name category name
     * @param description category description
     */
    public Category(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.level = 0;  // Default to root level
        this.sortOrder = 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating a hierarchical category.
     * 
     * @param categoryId unique identifier for the category
     * @param name category name
     * @param description category description
     * @param parentCategoryId ID of parent category
     * @param level depth level in hierarchy (0 = root)
     */
    public Category(String categoryId, String name, String description, 
                   String parentCategoryId, int level) {
        this(categoryId, name, description);
        this.parentCategoryId = parentCategoryId;
        this.level = level;
    }

    /**
     * Get the unique identifier for this category.
     * 
     * @return category ID
     */
    @DynamoDBHashKey(attributeName = "categoryId")
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Set the unique identifier for this category.
     * 
     * @param categoryId unique identifier to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Get the name of this category.
     * 
     * @return category name
     */
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    /**
     * Set the name of this category.
     * 
     * @param name category name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of this category.
     * 
     * @return category description
     */
    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this category.
     * 
     * @param description category description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the parent category ID if this is a subcategory.
     * 
     * @return parent category ID or null if this is a root category
     */
    @DynamoDBAttribute(attributeName = "parentCategoryId")
    public String getParentCategoryId() {
        return parentCategoryId;
    }

    /**
     * Set the parent category ID for this category.
     * 
     * @param parentCategoryId parent category ID to set
     */
    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    /**
     * Get the level of this category in the hierarchy (0 = root).
     * 
     * @return category level
     */
    @DynamoDBAttribute(attributeName = "level")
    public int getLevel() {
        return level;
    }

    /**
     * Set the level of this category in the hierarchy.
     * 
     * @param level category level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Get the display sort order of this category.
     * 
     * @return sort order value
     */
    @DynamoDBAttribute(attributeName = "sortOrder")
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Set the display sort order of this category.
     * 
     * @param sortOrder sort order value to set
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Check if this category is active.
     * 
     * @return true if the category is active, false otherwise
     */
    @DynamoDBAttribute(attributeName = "isActive")
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set whether this category is active.
     * 
     * @param active active status to set
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Get the creation timestamp of this category.
     * 
     * @return creation timestamp
     */
    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp of this category.
     * 
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last update timestamp of this category.
     * 
     * @return last update timestamp
     */
    @DynamoDBAttribute(attributeName = "updatedAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last update timestamp of this category.
     * 
     * @param updatedAt last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Determines if this category is a root level category.
     * 
     * @return true if this is a root category, false otherwise
     */
    public boolean isRootCategory() {
        return level == 0 || parentCategoryId == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Update the category's last modified timestamp.
     * Call this method before saving any updates to the category.
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // FIXME: Need to implement proper LocalDateTime converter for DynamoDB
    // TODO: Add indexing for efficient category hierarchy queries
}