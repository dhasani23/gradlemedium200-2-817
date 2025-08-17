package com.gradlemedium200.productcatalog.controller;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.dto.SearchRequestDto;
import com.gradlemedium200.productcatalog.dto.SearchResponseDto;
import com.gradlemedium200.productcatalog.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * REST controller for product search and filtering operations with advanced search capabilities.
 * This controller provides endpoints for searching products, getting search suggestions,
 * and performing quick searches.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    // Minimum query length for suggestions
    private static final int MIN_QUERY_LENGTH = 2;
    
    // Maximum results for quick search
    private static final int MAX_QUICK_SEARCH_RESULTS = 10;
    
    /**
     * Service for search operations
     */
    private final SearchService searchService;

    /**
     * Constructor with dependency injection
     *
     * @param searchService Service for search operations
     */
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search products with filters and pagination
     * 
     * @param searchRequest DTO containing search criteria and pagination info
     * @return ResponseEntity with search results and metadata
     */
    @PostMapping
    public ResponseEntity<SearchResponseDto> searchProducts(@Valid @RequestBody SearchRequestDto searchRequest) {
        logger.info("Received search request: {}", searchRequest);
        
        try {
            // Validate search request
            if (searchRequest == null) {
                logger.warn("Search request is null");
                return ResponseEntity.badRequest().build();
            }
            
            // Ensure page parameters are valid
            if (!searchRequest.isValidPageRequest()) {
                logger.warn("Invalid page request parameters: page={}, size={}", 
                        searchRequest.getPage(), searchRequest.getSize());
                return ResponseEntity.badRequest().build();
            }
            
            // Perform search using the service
            SearchResponseDto searchResponse = searchService.searchProducts(searchRequest);
            
            // Log search results summary
            logger.info("Search completed. Found {} products, returned page {} of {}",
                    searchResponse.getTotalElements(),
                    searchResponse.getCurrentPage() + 1,
                    searchResponse.getTotalPages());
            
            return ResponseEntity.ok(searchResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search request parameters", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error processing search request", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get search suggestions for autocomplete functionality
     * 
     * @param query The partial search query
     * @return ResponseEntity with a list of suggested search terms
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam(required = false) String query) {
        try {
            // Validate query length
            if (!StringUtils.hasText(query) || query.length() < MIN_QUERY_LENGTH) {
                logger.debug("Query too short for suggestions: '{}'", query);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            logger.debug("Getting search suggestions for query: '{}'", query);
            
            // Get suggestions from service
            List<String> suggestions = searchService.getSearchSuggestions(query);
            
            logger.debug("Returning {} search suggestions for query: '{}'", 
                    suggestions.size(), query);
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Error generating search suggestions for query: '{}'", query, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Quick product search for live search functionality
     * Returns a simplified list of products for immediate display
     * 
     * @param query The search query
     * @return ResponseEntity with a list of matching products (limited quantity)
     */
    @GetMapping("/quick")
    public ResponseEntity<List<ProductDto>> quickSearch(@RequestParam(required = false) String query) {
        try {
            // Validate query
            if (!StringUtils.hasText(query) || query.length() < MIN_QUERY_LENGTH) {
                logger.debug("Query too short for quick search: '{}'", query);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            logger.debug("Performing quick search for query: '{}'", query);
            
            // Create a simplified search request for quick results
            SearchRequestDto quickSearchRequest = new SearchRequestDto();
            quickSearchRequest.setQuery(query);
            quickSearchRequest.setPage(0);
            quickSearchRequest.setSize(MAX_QUICK_SEARCH_RESULTS);
            
            // Execute search
            SearchResponseDto searchResponse = searchService.searchProducts(quickSearchRequest);
            
            // Log results
            logger.debug("Quick search for '{}' returned {} results", 
                    query, searchResponse.getProducts().size());
            
            return ResponseEntity.ok(searchResponse.getProducts());
        } catch (Exception e) {
            logger.error("Error performing quick search for query: '{}'", query, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // TODO: Add endpoint for faceted search with dynamic facet generation
    
    // TODO: Implement caching for frequent search results to improve performance
    
    // FIXME: The quick search endpoint should be optimized for performance - consider separate implementation
}