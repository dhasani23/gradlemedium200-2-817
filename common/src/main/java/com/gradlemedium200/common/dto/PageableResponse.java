package com.gradlemedium200.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * PageableResponse - Paginated response DTO for list operations
 * 
 * This class provides a standardized structure for paginated API responses
 * across the application. It encapsulates the pagination metadata along
 * with the actual content being returned.
 * 
 * @author gradlemedium200
 * @version 1.0
 */
public class PageableResponse extends BaseDTO {
    
    private static final long serialVersionUID = 2L;
    
    /**
     * List of data items for the current page
     */
    private List<Object> content;
    
    /**
     * Total number of elements across all pages
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Current page number (0-based indexing)
     */
    private int pageNumber;
    
    /**
     * Size of each page (number of items per page)
     */
    private int pageSize;
    
    /**
     * Flag indicating if there is a next page available
     */
    private boolean hasNext;
    
    /**
     * Flag indicating if there is a previous page available
     */
    private boolean hasPrevious;
    
    /**
     * Default constructor initializing an empty content list
     */
    public PageableResponse() {
        this.content = new ArrayList<>();
    }
    
    /**
     * Constructor with content list
     * 
     * @param content the list of content items
     */
    public PageableResponse(List<Object> content) {
        this.content = content != null ? content : new ArrayList<>();
    }
    
    /**
     * Full constructor with all pagination parameters
     * 
     * @param content the list of content items
     * @param totalElements total number of elements across all pages
     * @param totalPages total number of pages
     * @param pageNumber current page number (0-based)
     * @param pageSize number of items per page
     */
    public PageableResponse(List<Object> content, long totalElements, int totalPages,
                           int pageNumber, int pageSize) {
        this.content = content != null ? content : new ArrayList<>();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        
        // Calculate navigation flags
        this.hasNext = pageNumber < totalPages - 1;
        this.hasPrevious = pageNumber > 0;
    }
    
    /**
     * Get the content list
     * 
     * @return List of data items for the current page
     */
    public List<Object> getContent() {
        return content;
    }
    
    /**
     * Set the content list
     * 
     * @param content List of data items to set
     */
    public void setContent(List<Object> content) {
        this.content = content != null ? content : new ArrayList<>();
    }
    
    /**
     * Get total elements count across all pages
     * 
     * @return total number of elements
     */
    public long getTotalElements() {
        return totalElements;
    }
    
    /**
     * Set total elements count
     * 
     * @param totalElements total number of elements to set
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    /**
     * Get total number of pages
     * 
     * @return total pages
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Set total number of pages
     * 
     * @param totalPages total pages to set
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    /**
     * Get current page number
     * 
     * @return current page number (0-based)
     */
    public int getPageNumber() {
        return pageNumber;
    }
    
    /**
     * Set current page number
     * 
     * @param pageNumber page number to set
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        // Update navigation flags
        this.hasPrevious = pageNumber > 0;
        this.hasNext = pageNumber < totalPages - 1;
    }
    
    /**
     * Get page size
     * 
     * @return size of each page
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Set page size
     * 
     * @param pageSize page size to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        // FIXME: Recalculate totalPages when pageSize changes
    }
    
    /**
     * Check if next page exists
     * 
     * @return true if there is a next page, false otherwise
     */
    public boolean isHasNext() {
        return hasNext;
    }
    
    /**
     * Set has next page flag
     * 
     * @param hasNext has next page flag to set
     */
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    /**
     * Check if previous page exists
     * 
     * @return true if there is a previous page, false otherwise
     */
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    /**
     * Set has previous page flag
     * 
     * @param hasPrevious has previous page flag to set
     */
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    /**
     * Helper method to check if content is empty
     * 
     * @return true if content is empty, false otherwise
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    /**
     * Helper method to get the size of the content
     * 
     * @return number of items in the content
     */
    public int size() {
        return content == null ? 0 : content.size();
    }
    
    /**
     * Recalculates the pagination metadata based on the current state
     * 
     * @return this object for method chaining
     */
    public PageableResponse recalculate() {
        // Calculate total pages based on totalElements and pageSize
        if (pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        } else {
            // TODO: Handle case when pageSize is invalid
            this.totalPages = totalElements > 0 ? 1 : 0;
        }
        
        // Update navigation flags
        this.hasNext = pageNumber < totalPages - 1;
        this.hasPrevious = pageNumber > 0;
        
        return this;
    }
    
    @Override
    public String toString() {
        return "PageableResponse [content.size=" + (content != null ? content.size() : 0) + 
               ", totalElements=" + totalElements + 
               ", totalPages=" + totalPages + 
               ", pageNumber=" + pageNumber + 
               ", pageSize=" + pageSize + 
               ", hasNext=" + hasNext + 
               ", hasPrevious=" + hasPrevious + 
               ", " + super.toString() + "]";
    }
}