package com.gradlemedium200.productcatalog.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Data transfer object for search request parameters including filters and sorting options.
 * This class encapsulates all search criteria that can be used to filter and sort products
 * in the product catalog.
 */
public class SearchRequestDto {
    
    // Search query field
    private String query;
    
    // Filter fields
    private String categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Set<String> tags;
    private boolean inStockOnly;
    
    // Sorting fields
    private String sortBy;  // price, name, popularity, rating
    private String sortOrder;  // ASC, DESC
    
    // Pagination fields
    private int page;
    private int size;
    
    /**
     * Default constructor
     */
    public SearchRequestDto() {
        // Default values for pagination
        this.page = 0;
        this.size = 20;
        this.inStockOnly = false;
    }

    /**
     * Get the search query string
     * 
     * @return the search query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Set the search query string
     * 
     * @param query the search query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Get the category ID filter
     * 
     * @return the categoryId
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Set the category ID filter
     * 
     * @param categoryId the categoryId to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Get the minimum price filter
     * 
     * @return the minPrice
     */
    public BigDecimal getMinPrice() {
        return minPrice;
    }

    /**
     * Set the minimum price filter
     * 
     * @param minPrice the minPrice to set
     */
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    /**
     * Get the maximum price filter
     * 
     * @return the maxPrice
     */
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    /**
     * Set the maximum price filter
     * 
     * @param maxPrice the maxPrice to set
     */
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    /**
     * Get the brand filter
     * 
     * @return the brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Set the brand filter
     * 
     * @param brand the brand to set
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Get the tag filters
     * 
     * @return the tags
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Set the tag filters
     * 
     * @param tags the tags to set
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Check if only in-stock products should be returned
     * 
     * @return true if only in-stock products should be included
     */
    public boolean isInStockOnly() {
        return inStockOnly;
    }

    /**
     * Set whether to filter for in-stock products only
     * 
     * @param inStockOnly true to include only in-stock products
     */
    public void setInStockOnly(boolean inStockOnly) {
        this.inStockOnly = inStockOnly;
    }

    /**
     * Get the sort field
     * 
     * @return the sortBy field name
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Set the sort field
     * 
     * @param sortBy the field to sort by (price, name, popularity, rating)
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Get the sort order
     * 
     * @return the sortOrder (ASC, DESC)
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Set the sort order
     * 
     * @param sortOrder the sortOrder to set (ASC, DESC)
     */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Get the page number for pagination
     * 
     * @return the page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Set the page number for pagination
     * 
     * @param page the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Get the page size for pagination
     * 
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the page size for pagination
     * 
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Check if any filters are applied in this search request
     * 
     * @return true if at least one filter is applied
     */
    public boolean hasFilters() {
        return categoryId != null || 
               minPrice != null || 
               maxPrice != null || 
               brand != null || 
               (tags != null && !tags.isEmpty()) ||
               inStockOnly || 
               !isEmpty(query);
    }
    
    /**
     * Validate if the pagination parameters are valid
     * 
     * @return true if pagination parameters are valid
     */
    public boolean isValidPageRequest() {
        return page >= 0 && size > 0 && size <= 100;  // Limiting max page size to prevent performance issues
    }
    
    /**
     * Helper method to check if a string is null or empty
     * 
     * @param str the string to check
     * @return true if the string is null or empty
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SearchRequestDto that = (SearchRequestDto) o;
        
        return inStockOnly == that.inStockOnly &&
               page == that.page &&
               size == that.size &&
               Objects.equals(query, that.query) &&
               Objects.equals(categoryId, that.categoryId) &&
               Objects.equals(minPrice, that.minPrice) &&
               Objects.equals(maxPrice, that.maxPrice) &&
               Objects.equals(brand, that.brand) &&
               Objects.equals(tags, that.tags) &&
               Objects.equals(sortBy, that.sortBy) &&
               Objects.equals(sortOrder, that.sortOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            query, categoryId, minPrice, maxPrice, brand, 
            tags, inStockOnly, sortBy, sortOrder, page, size
        );
    }

    @Override
    public String toString() {
        return "SearchRequestDto{" +
               "query='" + query + '\'' +
               ", categoryId='" + categoryId + '\'' +
               ", minPrice=" + minPrice +
               ", maxPrice=" + maxPrice +
               ", brand='" + brand + '\'' +
               ", tags=" + tags +
               ", inStockOnly=" + inStockOnly +
               ", sortBy='" + sortBy + '\'' +
               ", sortOrder='" + sortOrder + '\'' +
               ", page=" + page +
               ", size=" + size +
               '}';
    }
}