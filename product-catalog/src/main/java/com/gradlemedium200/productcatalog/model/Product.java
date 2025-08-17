package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Entity model representing a product in the product catalog system.
 * This class is annotated with DynamoDB annotations for ORM mapping to DynamoDB table.
 */
@DynamoDBTable(tableName = "Products")
public class Product {

    /**
     * Unique identifier for the product
     */
    private String productId;
    
    /**
     * Name of the product
     */
    private String name;
    
    /**
     * Detailed description of the product
     */
    private String description;
    
    /**
     * ID of the category this product belongs to
     */
    private String categoryId;
    
    /**
     * Price of the product
     */
    private BigDecimal price;
    
    /**
     * Currency code for the price (e.g., USD, EUR)
     */
    private String currency;
    
    /**
     * Stock Keeping Unit identifier
     */
    private String sku;
    
    /**
     * Product brand
     */
    private String brand;
    
    /**
     * List of product image URLs
     */
    private List<String> imageUrls;
    
    /**
     * Product tags for search and categorization
     */
    private Set<String> tags;
    
    /**
     * Current inventory count
     */
    private Integer inventory;
    
    /**
     * Product status (ACTIVE, INACTIVE, DISCONTINUED)
     */
    private String status;
    
    /**
     * Product creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Product last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by DynamoDB mapper
     */
    public Product() {
    }

    /**
     * Constructor with essential fields
     * 
     * @param productId unique identifier for the product
     * @param name product name
     * @param price product price
     * @param currency currency code for the price
     */
    public Product(String productId, String name, BigDecimal price, String currency) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get the product ID
     * 
     * @return the product ID
     */
    @DynamoDBHashKey(attributeName = "ProductId")
    public String getProductId() {
        return productId;
    }

    /**
     * Set the product ID
     * 
     * @param productId the product ID to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get the product name
     * 
     * @return the product name
     */
    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {
        return name;
    }

    /**
     * Set the product name
     * 
     * @param name the product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the product description
     * 
     * @return the product description
     */
    @DynamoDBAttribute(attributeName = "Description")
    public String getDescription() {
        return description;
    }

    /**
     * Set the product description
     * 
     * @param description the product description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the category ID
     * 
     * @return the category ID
     */
    @DynamoDBAttribute(attributeName = "CategoryId")
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Set the category ID
     * 
     * @param categoryId the category ID to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Get the product price
     * 
     * @return the product price
     */
    @DynamoDBAttribute(attributeName = "Price")
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Set the product price
     * 
     * @param price the product price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Get the currency code
     * 
     * @return the currency code
     */
    @DynamoDBAttribute(attributeName = "Currency")
    public String getCurrency() {
        return currency;
    }

    /**
     * Set the currency code
     * 
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Get the SKU (Stock Keeping Unit)
     * 
     * @return the SKU
     */
    @DynamoDBAttribute(attributeName = "SKU")
    public String getSku() {
        return sku;
    }

    /**
     * Set the SKU (Stock Keeping Unit)
     * 
     * @param sku the SKU to set
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Get the product brand
     * 
     * @return the product brand
     */
    @DynamoDBAttribute(attributeName = "Brand")
    public String getBrand() {
        return brand;
    }

    /**
     * Set the product brand
     * 
     * @param brand the product brand to set
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Get the list of image URLs
     * 
     * @return the list of image URLs
     */
    @DynamoDBAttribute(attributeName = "ImageUrls")
    public List<String> getImageUrls() {
        return imageUrls;
    }

    /**
     * Set the list of image URLs
     * 
     * @param imageUrls the list of image URLs to set
     */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * Get the product tags
     * 
     * @return the product tags
     */
    @DynamoDBAttribute(attributeName = "Tags")
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Set the product tags
     * 
     * @param tags the product tags to set
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Get the inventory count
     * 
     * @return the inventory count
     */
    @DynamoDBAttribute(attributeName = "Inventory")
    public Integer getInventory() {
        return inventory;
    }

    /**
     * Set the inventory count
     * 
     * @param inventory the inventory count to set
     */
    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    /**
     * Get the product status
     * 
     * @return the product status
     */
    @DynamoDBAttribute(attributeName = "Status")
    public String getStatus() {
        return status;
    }

    /**
     * Set the product status
     * 
     * @param status the product status to set
     */
    public void setStatus(String status) {
        this.status = status;
        
        // Update the updatedAt timestamp whenever status changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if product is active
     * 
     * @return true if the product status is "ACTIVE", false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Get the creation timestamp
     * 
     * @return the creation timestamp
     */
    @DynamoDBAttribute(attributeName = "CreatedAt")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp
     * 
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last update timestamp
     * 
     * @return the last update timestamp
     */
    @DynamoDBAttribute(attributeName = "UpdatedAt")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last update timestamp
     * 
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 20)) + "..." : null) + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    // TODO: Add custom serialization/deserialization for LocalDateTime since DynamoDB 
    // doesn't natively support Java 8 time classes
    
    // TODO: Implement inventory synchronization mechanism with the inventory service
    
    // FIXME: Need to address potential performance issues with large image URL lists
}