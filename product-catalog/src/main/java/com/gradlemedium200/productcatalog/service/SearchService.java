package com.gradlemedium200.productcatalog.service;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.dto.SearchRequestDto;
import com.gradlemedium200.productcatalog.dto.SearchResponseDto;
import com.gradlemedium200.productcatalog.model.Product;
import com.gradlemedium200.productcatalog.repository.ProductRepository;
import com.gradlemedium200.productcatalog.repository.SearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for product search, filtering, and sorting operations with advanced search capabilities.
 * This service provides methods for searching products with various criteria, filtering, sorting,
 * and generating search suggestions for autocomplete functionality.
 */
@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
    // Maximum number of search suggestions to return
    private static final int MAX_SUGGESTIONS = 10;
    
    // Repository for search operations
    private final SearchRepository searchRepository;
    
    // Repository for product data access
    private final ProductRepository productRepository;

    /**
     * Constructor with dependency injection
     *
     * @param searchRepository Repository for search operations
     * @param productRepository Repository for product data access
     */
    @Autowired
    public SearchService(SearchRepository searchRepository, ProductRepository productRepository) {
        this.searchRepository = searchRepository;
        this.productRepository = productRepository;
    }

    /**
     * Search products with filters and pagination
     *
     * @param searchRequest DTO containing search criteria and pagination info
     * @return SearchResponseDto containing search results and metadata
     */
    public SearchResponseDto searchProducts(SearchRequestDto searchRequest) {
        logger.info("Searching products with criteria: {}", searchRequest);
        
        if (searchRequest == null) {
            logger.warn("Search request is null, returning empty response");
            return new SearchResponseDto(Collections.emptyList(), 0, 0, 0, 0);
        }
        
        if (!searchRequest.isValidPageRequest()) {
            logger.warn("Invalid page request: page={}, size={}", searchRequest.getPage(), searchRequest.getSize());
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        
        // Track search execution time
        long startTime = System.currentTimeMillis();
        
        // Perform the search
        List<Product> matchingProducts = performSearch(searchRequest);
        
        // Apply additional filters if needed
        if (searchRequest.hasFilters()) {
            matchingProducts = applyAdditionalFilters(matchingProducts, searchRequest);
        }
        
        // Calculate total count and pages
        int totalElements = matchingProducts.size();
        int pageSize = searchRequest.getSize();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Apply pagination
        List<Product> pagedProducts = applyPagination(matchingProducts, searchRequest.getPage(), pageSize);
        
        // Convert products to DTOs
        List<ProductDto> productDtos = convertToProductDtos(pagedProducts);
        
        // Apply sorting if specified
        if (StringUtils.hasText(searchRequest.getSortBy())) {
            productDtos = sortProducts(productDtos, searchRequest.getSortBy(), searchRequest.getSortOrder());
        }
        
        // Calculate search time
        long searchTime = System.currentTimeMillis() - startTime;
        
        // Create and populate response DTO
        SearchResponseDto response = new SearchResponseDto(
                productDtos,
                totalElements,
                totalPages,
                searchRequest.getPage(),
                pageSize
        );
        
        // Set search execution time
        response.setSearchTime(searchTime);
        
        // Add facets for filtering (simplified version)
        response.setFacets(generateFacets(matchingProducts));
        
        logger.info("Search completed in {}ms, found {} products, returning page {} of {}", 
                searchTime, totalElements, searchRequest.getPage(), totalPages);
        
        return response;
    }

    /**
     * Get search suggestions for autocomplete functionality
     *
     * @param query The partial search query
     * @return List of suggested search terms
     */
    public List<String> getSearchSuggestions(String query) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return Collections.emptyList();
        }
        
        logger.debug("Generating search suggestions for query: {}", query);
        
        // Get all active products
        List<Product> products = productRepository.findByStatus("ACTIVE");
        
        // Create a list to hold potential suggestions
        Set<String> suggestions = new HashSet<>();
        
        String queryLowerCase = query.toLowerCase();
        
        // Extract potential suggestions from product names
        for (Product product : products) {
            // Add whole product name if it contains the query
            if (product.getName().toLowerCase().contains(queryLowerCase)) {
                suggestions.add(product.getName());
            }
            
            // Add individual words from product names
            String[] nameParts = product.getName().split("\\s+");
            for (String part : nameParts) {
                if (part.toLowerCase().contains(queryLowerCase)) {
                    suggestions.add(part);
                }
            }
            
            // Add brand name if it contains the query
            if (product.getBrand() != null && product.getBrand().toLowerCase().contains(queryLowerCase)) {
                suggestions.add(product.getBrand());
            }
            
            // Add relevant tags that contain the query
            if (product.getTags() != null) {
                for (String tag : product.getTags()) {
                    if (tag.toLowerCase().contains(queryLowerCase)) {
                        suggestions.add(tag);
                    }
                }
            }
        }
        
        // Sort suggestions by relevance and limit to MAX_SUGGESTIONS
        return suggestions.stream()
                .sorted((s1, s2) -> {
                    // Prioritize exact matches and starts-with matches
                    boolean s1Exact = s1.equalsIgnoreCase(query);
                    boolean s2Exact = s2.equalsIgnoreCase(query);
                    
                    if (s1Exact && !s2Exact) return -1;
                    if (!s1Exact && s2Exact) return 1;
                    
                    boolean s1StartsWith = s1.toLowerCase().startsWith(queryLowerCase);
                    boolean s2StartsWith = s2.toLowerCase().startsWith(queryLowerCase);
                    
                    if (s1StartsWith && !s2StartsWith) return -1;
                    if (!s1StartsWith && s2StartsWith) return 1;
                    
                    // Default to alphabetical order
                    return s1.compareToIgnoreCase(s2);
                })
                .limit(MAX_SUGGESTIONS)
                .collect(Collectors.toList());
    }

    /**
     * Apply filters to a list of products
     *
     * @param products List of products to filter
     * @param filters Map of filter criteria (key = filter name, value = filter value)
     * @return Filtered list of products
     */
    public List<ProductDto> filterProducts(List<ProductDto> products, Map<String, Object> filters) {
        if (products == null || filters == null || filters.isEmpty()) {
            return products;
        }
        
        logger.debug("Applying filters to {} products: {}", products.size(), filters);
        
        return products.stream()
                .filter(product -> matchesFilters(product, filters))
                .collect(Collectors.toList());
    }

    /**
     * Sort products by specified criteria
     *
     * @param products List of products to sort
     * @param sortBy Field to sort by
     * @param sortOrder Sort direction (ASC or DESC)
     * @return Sorted list of products
     */
    public List<ProductDto> sortProducts(List<ProductDto> products, String sortBy, String sortOrder) {
        if (products == null || products.isEmpty() || !StringUtils.hasText(sortBy)) {
            return products;
        }
        
        boolean ascending = !"DESC".equalsIgnoreCase(sortOrder); // Default to ascending if not specified as DESC
        
        logger.debug("Sorting {} products by {} ({})", products.size(), sortBy, ascending ? "ASC" : "DESC");
        
        Comparator<ProductDto> comparator;
        
        // Create appropriate comparator based on sort field
        switch (sortBy.toLowerCase()) {
            case "price":
                comparator = Comparator.comparing(
                        ProductDto::getEffectivePrice, 
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                break;
            case "name":
                comparator = Comparator.comparing(
                        ProductDto::getName, 
                        Comparator.nullsLast(String::compareToIgnoreCase)
                );
                break;
            // Assuming we have a popularity field (simplified here)
            case "popularity":
                // In a real implementation, this would use a specific popularity metric
                // For now, we'll just use the product ID as a placeholder
                comparator = Comparator.comparing(ProductDto::getProductId);
                break;
            // Assuming we have a rating field
            case "rating":
                // This would require a rating field in the DTO, using price as a placeholder
                comparator = Comparator.comparing(
                        ProductDto::getPrice, 
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                break;
            default:
                logger.warn("Unsupported sort field: {}, defaulting to name", sortBy);
                comparator = Comparator.comparing(
                        ProductDto::getName, 
                        Comparator.nullsLast(String::compareToIgnoreCase)
                );
        }
        
        // Apply sort direction
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        // Perform sorting
        return products.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Performs the actual search operation using the search repository
     *
     * @param searchRequest The search request containing search criteria
     * @return List of products matching the search criteria
     */
    private List<Product> performSearch(SearchRequestDto searchRequest) {
        // If tags are provided, perform tag-based search
        if (searchRequest.getTags() != null && !searchRequest.getTags().isEmpty()) {
            return searchRepository.searchByTags(searchRequest.getTags());
        }
        
        // Otherwise perform regular search
        return searchRepository.searchProducts(
                searchRequest.getQuery(),
                searchRequest.getCategoryId(),
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice()
        );
    }

    /**
     * Apply additional filters not handled by the initial repository search
     *
     * @param products List of products to filter
     * @param searchRequest Search request containing filter criteria
     * @return Filtered list of products
     */
    private List<Product> applyAdditionalFilters(List<Product> products, SearchRequestDto searchRequest) {
        return products.stream()
                .filter(product -> {
                    // Apply brand filter
                    if (StringUtils.hasText(searchRequest.getBrand()) && 
                            !searchRequest.getBrand().equals(product.getBrand())) {
                        return false;
                    }
                    
                    // Apply in-stock filter
                    if (searchRequest.isInStockOnly() && 
                            (product.getInventory() == null || product.getInventory() <= 0)) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply pagination to a list of products
     *
     * @param products Full list of products
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Sublist representing the requested page
     */
    private List<Product> applyPagination(List<Product> products, int page, int size) {
        int fromIndex = page * size;
        
        if (fromIndex >= products.size()) {
            return Collections.emptyList();
        }
        
        int toIndex = Math.min(fromIndex + size, products.size());
        
        return products.subList(fromIndex, toIndex);
    }

    /**
     * Convert Product entities to ProductDto objects
     *
     * @param products List of Product entities
     * @return List of ProductDto objects
     */
    private List<ProductDto> convertToProductDtos(List<Product> products) {
        // In a real implementation, this would use a dedicated mapper
        // This is a simplified version
        
        return products.stream()
                .map(product -> {
                    ProductDto dto = new ProductDto();
                    dto.setProductId(product.getProductId());
                    dto.setName(product.getName());
                    dto.setDescription(product.getDescription());
                    dto.setCategoryId(product.getCategoryId());
                    dto.setPrice(product.getPrice());
                    dto.setCurrency(product.getCurrency());
                    
                    // Set in stock status based on inventory
                    dto.setInStock(product.getInventory() != null && product.getInventory() > 0);
                    
                    // Set tags
                    if (product.getTags() != null) {
                        dto.setTags(new HashSet<>(product.getTags()));
                    }
                    
                    // Set image URLs
                    if (product.getImageUrls() != null) {
                        dto.setImageUrls(new ArrayList<>(product.getImageUrls()));
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if a product matches all filters in the filter map
     *
     * @param product Product to check
     * @param filters Map of filter criteria
     * @return true if the product matches all filters, false otherwise
     */
    private boolean matchesFilters(ProductDto product, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            Object value = filter.getValue();
            
            switch (key.toLowerCase()) {
                case "category":
                    String categoryId = value.toString();
                    if (!categoryId.equals(product.getCategoryId())) {
                        return false;
                    }
                    break;
                case "brand":
                    // Assuming brand was added to ProductDto
                    // This would need to be implemented properly
                    break;
                case "instock":
                    boolean inStockRequired = Boolean.parseBoolean(value.toString());
                    if (inStockRequired && !product.isInStock()) {
                        return false;
                    }
                    break;
                case "minprice":
                    if (value instanceof BigDecimal) {
                        BigDecimal minPrice = (BigDecimal) value;
                        if (product.getEffectivePrice().compareTo(minPrice) < 0) {
                            return false;
                        }
                    }
                    break;
                case "maxprice":
                    if (value instanceof BigDecimal) {
                        BigDecimal maxPrice = (BigDecimal) value;
                        if (product.getEffectivePrice().compareTo(maxPrice) > 0) {
                            return false;
                        }
                    }
                    break;
                case "tag":
                    String tag = value.toString();
                    if (product.getTags() == null || !product.getTags().contains(tag)) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }

    /**
     * Generate facets from a list of products for faceted search
     *
     * @param products List of products to analyze
     * @return Map of facet names to possible values
     */
    private Map<String, List<String>> generateFacets(List<Product> products) {
        Map<String, List<String>> facets = new HashMap<>();
        
        // Extract categories
        List<String> categories = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        facets.put("categories", categories);
        
        // Extract brands
        List<String> brands = products.stream()
                .map(Product::getBrand)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        facets.put("brands", brands);
        
        // Extract tags (top 20 most common)
        Map<String, Long> tagCounts = new HashMap<>();
        products.stream()
                .filter(p -> p.getTags() != null)
                .flatMap(p -> p.getTags().stream())
                .forEach(tag -> {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0L) + 1);
                });
        
        List<String> topTags = tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        facets.put("tags", topTags);
        
        return facets;
    }
    
    // TODO: Implement caching for frequent search queries to improve performance
    
    // TODO: Add support for fuzzy search to handle typos and spelling variations
    
    // FIXME: The current filtering approach is inefficient for large datasets.
    // Consider moving more filtering logic to the database query level.
}