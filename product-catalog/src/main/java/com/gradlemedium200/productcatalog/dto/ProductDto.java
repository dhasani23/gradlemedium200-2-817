package com.gradlemedium200.productcatalog.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data transfer object for product information used in API requests and responses.
 * This DTO is used to transfer product data between the client and the server,
 * hiding internal implementation details of the product entity.
 */
public class ProductDto {

    /**
     * Unique identifier for the product
     */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /**
     * Name of the product
     */
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    /**
     * Detailed description of the product
     */
    private String description;

    /**
     * Category identifier to which this product belongs
     */
    @NotNull(message = "Category ID cannot be null")
    private String categoryId;

    /**
     * Display name of the category
     */
    private String categoryName;

    /**
     * Regular price of the product
     */
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;

    /**
     * Sale price of the product if it's on sale
     */
    private BigDecimal salePrice;

    /**
     * Currency code for the price (e.g., USD, EUR)
     */
    @NotBlank(message = "Currency cannot be blank")
    private String currency;

    /**
     * List of URLs for product images
     */
    private List<String> imageUrls = new ArrayList<>();

    /**
     * Flag indicating if the product is currently in stock
     */
    private boolean inStock;

    /**
     * Set of tags/labels associated with this product for search and filtering
     */
    private Set<String> tags = new HashSet<>();

    /**
     * Default constructor
     */
    public ProductDto() {
        // Default constructor required for serialization/deserialization
    }

    /**
     * Constructor with essential fields
     * 
     * @param productId unique product identifier
     * @param name product name
     * @param price product price
     * @param currency price currency
     */
    public ProductDto(String productId, String name, BigDecimal price, String currency) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.currency = currency;
    }

    /**
     * Returns the product identifier
     * 
     * @return product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product identifier
     * 
     * @param productId product ID to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the product name
     * 
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name
     * 
     * @param name product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product description
     * 
     * @return product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the product description
     * 
     * @param description product description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the category ID
     * 
     * @return category identifier
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category ID
     * 
     * @param categoryId category identifier to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Returns the category name
     * 
     * @return category name
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the category name
     * 
     * @param categoryName category name to set
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Returns the product price
     * 
     * @return product price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the product price
     * 
     * @param price product price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the sale price if applicable
     * 
     * @return sale price
     */
    public BigDecimal getSalePrice() {
        return salePrice;
    }

    /**
     * Sets the sale price
     * 
     * @param salePrice sale price to set
     */
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    /**
     * Returns the currency code
     * 
     * @return currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code
     * 
     * @param currency currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the list of image URLs
     * 
     * @return list of image URLs
     */
    public List<String> getImageUrls() {
        return imageUrls;
    }

    /**
     * Sets the list of image URLs
     * 
     * @param imageUrls list of image URLs to set
     */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    /**
     * Adds a new image URL to the list
     * 
     * @param imageUrl image URL to add
     */
    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        this.imageUrls.add(imageUrl);
    }

    /**
     * Returns whether the product is in stock
     * 
     * @return true if product is in stock, false otherwise
     */
    public boolean isInStock() {
        return inStock;
    }

    /**
     * Sets the stock status
     * 
     * @param inStock stock status to set
     */
    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    /**
     * Returns the set of tags
     * 
     * @return set of product tags
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Sets the product tags
     * 
     * @param tags set of tags to set
     */
    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    /**
     * Adds a new tag to the product
     * 
     * @param tag tag to add
     */
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);
    }

    /**
     * Checks if the product is on sale
     * 
     * @return true if the product has a sale price that is different from the regular price
     */
    public boolean isOnSale() {
        return salePrice != null && price != null && salePrice.compareTo(price) < 0;
    }

    /**
     * Returns the effective price (sale price if available, otherwise regular price)
     * 
     * @return the effective price
     */
    public BigDecimal getEffectivePrice() {
        return isOnSale() ? salePrice : price;
    }

    /**
     * Returns string representation of ProductDto
     */
    @Override
    public String toString() {
        return "ProductDto{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", price=" + price +
                ", salePrice=" + salePrice +
                ", currency='" + currency + '\'' +
                ", inStock=" + inStock +
                ", tagsCount=" + (tags != null ? tags.size() : 0) +
                ", imageCount=" + (imageUrls != null ? imageUrls.size() : 0) +
                '}';
    }

    // TODO: Consider adding equals and hashCode methods for proper object comparison

    // FIXME: Handle null values properly in getEffectivePrice to avoid potential NPE
}