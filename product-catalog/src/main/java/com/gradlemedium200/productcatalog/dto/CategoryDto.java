package com.gradlemedium200.productcatalog.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

/**
 * Data transfer object for category information used in API operations.
 * Represents a product category with its attributes and hierarchical relationships.
 */
public class CategoryDto {
    
    @NotBlank(message = "Category ID is required")
    private String categoryId;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    private String parentCategoryId;
    private int level;
    private long productCount;
    private int sortOrder;
    private List<CategoryDto> subcategories;
    
    /**
     * Default constructor initializing the subcategories list
     */
    public CategoryDto() {
        this.subcategories = new ArrayList<>();
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param categoryId The category's unique identifier
     * @param name The category name
     * @param description The category description
     */
    public CategoryDto(String categoryId, String name, String description) {
        this();
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }
    
    /**
     * Full constructor
     * 
     * @param categoryId The category's unique identifier
     * @param name The category name
     * @param description The category description
     * @param parentCategoryId The parent category ID
     * @param level The category hierarchy level
     * @param productCount Number of products in this category
     */
    public CategoryDto(String categoryId, String name, String description, 
                     String parentCategoryId, int level, long productCount) {
        this(categoryId, name, description);
        this.parentCategoryId = parentCategoryId;
        this.level = level;
        this.productCount = productCount;
    }
    
    /**
     * Get category ID
     * 
     * @return The category identifier
     */
    public String getCategoryId() {
        return categoryId;
    }
    
    /**
     * Set category ID
     * 
     * @param categoryId The category identifier
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * Get category name
     * 
     * @return The category name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set category name
     * 
     * @param name The category name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get category description
     * 
     * @return The category description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set category description
     * 
     * @param description The category description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get parent category ID
     * 
     * @return The parent category identifier
     */
    public String getParentCategoryId() {
        return parentCategoryId;
    }
    
    /**
     * Set parent category ID
     * 
     * @param parentCategoryId The parent category identifier
     */
    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
    
    /**
     * Get category hierarchy level
     * 
     * @return The category level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Set category hierarchy level
     * 
     * @param level The category level
     */
    public void setLevel(int level) {
        this.level = level;
    }
    
    /**
     * Get product count in this category
     * 
     * @return The number of products
     */
    public long getProductCount() {
        return productCount;
    }
    
    /**
     * Set product count in this category
     * 
     * @param productCount The number of products
     */
    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }
    
    /**
     * Get sort order for this category
     * 
     * @return The sort order value
     */
    public int getSortOrder() {
        return sortOrder;
    }
    
    /**
     * Set sort order for this category
     * 
     * @param sortOrder The sort order value
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    /**
     * Get list of subcategories
     * 
     * @return List of subcategories
     */
    public List<CategoryDto> getSubcategories() {
        return subcategories;
    }
    
    /**
     * Set list of subcategories
     * 
     * @param subcategories List of subcategories
     */
    public void setSubcategories(List<CategoryDto> subcategories) {
        this.subcategories = subcategories != null ? subcategories : new ArrayList<>();
    }
    
    /**
     * Add a subcategory to this category
     * 
     * @param subcategory The subcategory to add
     * @return true if added successfully
     */
    public boolean addSubcategory(CategoryDto subcategory) {
        if (subcategory == null) {
            return false;
        }
        
        // Set the parent ID and update the level if not already set
        if (subcategory.getParentCategoryId() == null) {
            subcategory.setParentCategoryId(this.categoryId);
        }
        
        if (subcategory.getLevel() == 0) {
            subcategory.setLevel(this.level + 1);
        }
        
        return this.subcategories.add(subcategory);
    }
    
    /**
     * Remove a subcategory from this category
     * 
     * @param subcategoryId The ID of the subcategory to remove
     * @return true if removed successfully
     */
    public boolean removeSubcategory(String subcategoryId) {
        if (subcategoryId == null) {
            return false;
        }
        
        return subcategories.removeIf(subcategory -> subcategoryId.equals(subcategory.getCategoryId()));
    }
    
    /**
     * Check if this category is a root category (no parent)
     * 
     * @return true if this is a root category
     */
    public boolean isRootCategory() {
        return parentCategoryId == null || parentCategoryId.isEmpty();
    }
    
    /**
     * Check if this category has subcategories
     * 
     * @return true if this category has subcategories
     */
    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        CategoryDto that = (CategoryDto) o;
        
        return categoryId != null ? categoryId.equals(that.categoryId) : that.categoryId == null;
    }
    
    @Override
    public int hashCode() {
        return categoryId != null ? categoryId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "CategoryDto{" +
                "categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", productCount=" + productCount +
                ", subcategories=" + subcategories.size() +
                '}';
    }
}