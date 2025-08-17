package com.gradlemedium200.productcatalog.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data transfer object for search response containing results and metadata.
 * This DTO encapsulates the results of a product search operation along with
 * pagination information, facets for filtering, and search performance metrics.
 */
public class SearchResponseDto {

    /**
     * List of matching products for the current page
     */
    private List<ProductDto> products;
    
    /**
     * Total number of products matching the search query across all pages
     */
    private long totalElements;
    
    /**
     * Total number of pages available for the search results
     */
    private int totalPages;
    
    /**
     * Current page number (zero-based)
     */
    private int currentPage;
    
    /**
     * Number of items per page
     */
    private int pageSize;
    
    /**
     * Time taken to execute the search query in milliseconds
     */
    private long searchTime;
    
    /**
     * Search facets for filtering, organized by facet name and possible values
     */
    private Map<String, List<String>> facets;

    /**
     * Default constructor
     */
    public SearchResponseDto() {
        this.products = new ArrayList<>();
    }

    /**
     * Constructor with essential fields
     * 
     * @param products List of products for the current page
     * @param totalElements Total number of matching products
     * @param totalPages Total number of pages
     * @param currentPage Current page number
     * @param pageSize Number of items per page
     */
    public SearchResponseDto(List<ProductDto> products, long totalElements, int totalPages, 
                           int currentPage, int pageSize) {
        this.products = products != null ? products : new ArrayList<>();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    /**
     * Get the list of products in the current page of search results
     * 
     * @return List of product DTOs
     */
    public List<ProductDto> getProducts() {
        return products != null ? products : Collections.emptyList();
    }

    /**
     * Set the list of products in the current page of search results
     * 
     * @param products List of product DTOs to set
     */
    public void setProducts(List<ProductDto> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    /**
     * Get the total number of elements matching the search query
     * 
     * @return Total number of matching products
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Set the total number of elements matching the search query
     * 
     * @param totalElements Total number of matching products
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Get the total number of pages available
     * 
     * @return Total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Set the total number of pages available
     * 
     * @param totalPages Total number of pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Get the current page number
     * 
     * @return Current page number (zero-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set the current page number
     * 
     * @param currentPage Current page number (zero-based)
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Get the number of items per page
     * 
     * @return Number of items per page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Set the number of items per page
     * 
     * @param pageSize Number of items per page
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Get the search execution time in milliseconds
     * 
     * @return Search time in milliseconds
     */
    public long getSearchTime() {
        return searchTime;
    }

    /**
     * Set the search execution time in milliseconds
     * 
     * @param searchTime Search time in milliseconds
     */
    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

    /**
     * Get the facets map for filtering search results
     * 
     * @return Map of facet names to possible values
     */
    public Map<String, List<String>> getFacets() {
        return facets;
    }

    /**
     * Set the facets map for filtering search results
     * 
     * @param facets Map of facet names to possible values
     */
    public void setFacets(Map<String, List<String>> facets) {
        this.facets = facets;
    }

    /**
     * Check if there are more pages available after the current page
     * 
     * @return true if there are more pages available, false otherwise
     */
    public boolean hasMore() {
        return currentPage < totalPages - 1;
    }

    /**
     * Check if the search returned any results
     * 
     * @return true if no results were found, false otherwise
     */
    public boolean isEmpty() {
        return products == null || products.isEmpty();
    }

    /**
     * Returns a string representation of the search response
     * 
     * @return String representation with key fields
     */
    @Override
    public String toString() {
        return "SearchResponseDto{" +
                "totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", productCount=" + (products != null ? products.size() : 0) +
                ", hasMore=" + hasMore() +
                ", searchTime=" + searchTime + "ms" +
                ", facetCount=" + (facets != null ? facets.size() : 0) +
                '}';
    }

    // TODO: Implement custom JSON serialization if needed for special formats

    // FIXME: Consider performance implications of hasMore() for large result sets
}