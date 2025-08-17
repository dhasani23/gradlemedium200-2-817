package com.gradlemedium200.productcatalog.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Data transfer object for paginated search results with comprehensive metadata.
 * This class provides a standardized format for returning search results with 
 * pagination information to clients, making it easier to navigate through large
 * result sets.
 */
public class PaginatedSearchResult {

    /**
     * List of products in the current page
     */
    private List<ProductDto> content;

    /**
     * Total number of elements across all pages
     */
    private long totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Current page number (0-based)
     */
    private int currentPage;

    /**
     * Number of elements per page
     */
    private int pageSize;

    /**
     * Whether this is the first page
     */
    private boolean isFirst;

    /**
     * Whether this is the last page
     */
    private boolean isLast;

    /**
     * Whether there is a next page
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;

    /**
     * Default constructor initializing with empty content
     */
    public PaginatedSearchResult() {
        this.content = new ArrayList<>();
    }

    /**
     * Constructor with essential pagination information
     * 
     * @param content List of products for the current page
     * @param totalElements Total number of elements across all pages
     * @param totalPages Total number of pages
     * @param currentPage Current page number (0-based)
     * @param pageSize Number of elements per page
     */
    public PaginatedSearchResult(List<ProductDto> content, long totalElements, int totalPages, 
                               int currentPage, int pageSize) {
        this.content = content != null ? content : new ArrayList<>();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        
        // Calculate derived pagination flags
        this.isFirst = currentPage == 0;
        this.isLast = currentPage == totalPages - 1 || totalPages == 0;
        this.hasNext = !isLast;
        this.hasPrevious = !isFirst;
    }

    /**
     * Get the content of the current page
     * 
     * @return List of product DTOs in the current page
     */
    public List<ProductDto> getContent() {
        return Collections.unmodifiableList(content);
    }

    /**
     * Set the content of the current page
     * 
     * @param content List of product DTOs
     */
    public void setContent(List<ProductDto> content) {
        this.content = content != null ? content : new ArrayList<>();
    }

    /**
     * Check if the current page is empty
     * 
     * @return true if the page has no content, false otherwise
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    /**
     * Get the number of elements in the current page
     * 
     * @return number of elements in the current page
     */
    public int getNumberOfElements() {
        return content == null ? 0 : content.size();
    }

    /**
     * Get total number of elements across all pages
     * 
     * @return total number of elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Set total number of elements across all pages
     * 
     * @param totalElements total number of elements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Get total number of pages
     * 
     * @return total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Set total number of pages
     * 
     * @param totalPages total number of pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        // Update isLast flag since it depends on totalPages
        this.isLast = currentPage == totalPages - 1 || totalPages == 0;
        this.hasNext = !isLast;
    }

    /**
     * Get current page number (0-based)
     * 
     * @return current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set current page number and update related flags
     * 
     * @param currentPage current page number (0-based)
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        // Update pagination flags that depend on currentPage
        this.isFirst = currentPage == 0;
        this.isLast = currentPage == totalPages - 1 || totalPages == 0;
        this.hasNext = !isLast;
        this.hasPrevious = !isFirst;
    }

    /**
     * Get number of elements per page
     * 
     * @return page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Set number of elements per page
     * 
     * @param pageSize number of elements per page
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Check if this is the first page
     * 
     * @return true if this is the first page, false otherwise
     */
    public boolean isFirst() {
        return isFirst;
    }

    /**
     * Set whether this is the first page
     * 
     * @param isFirst whether this is the first page
     */
    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
        // Update hasPrevious flag as it depends on isFirst
        this.hasPrevious = !isFirst;
    }

    /**
     * Check if this is the last page
     * 
     * @return true if this is the last page, false otherwise
     */
    public boolean isLast() {
        return isLast;
    }

    /**
     * Set whether this is the last page
     * 
     * @param isLast whether this is the last page
     */
    public void setLast(boolean isLast) {
        this.isLast = isLast;
        // Update hasNext flag as it depends on isLast
        this.hasNext = !isLast;
    }

    /**
     * Check if there is a next page
     * 
     * @return true if there is a next page, false otherwise
     */
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Set whether there is a next page
     * 
     * @param hasNext whether there is a next page
     */
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
        // Update isLast flag as it depends on hasNext
        this.isLast = !hasNext;
    }

    /**
     * Check if there is a previous page
     * 
     * @return true if there is a previous page, false otherwise
     */
    public boolean hasPrevious() {
        return hasPrevious;
    }

    /**
     * Set whether there is a previous page
     * 
     * @param hasPrevious whether there is a previous page
     */
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
        // Update isFirst flag as it depends on hasPrevious
        this.isFirst = !hasPrevious;
    }

    /**
     * Creates a string representation of the PaginatedSearchResult
     * 
     * @return String representation with pagination metadata
     */
    @Override
    public String toString() {
        return "PaginatedSearchResult{" +
                "content.size=" + (content != null ? content.size() : 0) +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", isFirst=" + isFirst +
                ", isLast=" + isLast +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }

    /**
     * Creates an empty result with zero elements and pages
     * 
     * @param pageSize the size of a page
     * @return empty paginated result
     */
    public static PaginatedSearchResult emptyResult(int pageSize) {
        return new PaginatedSearchResult(
            Collections.emptyList(), 0, 0, 0, pageSize
        );
    }

    // TODO: Add helper methods for creating page navigation links
    
    // FIXME: Ensure consistency between hasNext/hasPrevious and isFirst/isLast flags when modified directly
}