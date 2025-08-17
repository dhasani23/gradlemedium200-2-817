package com.gradlemedium200.productcatalog.controller;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.dto.RecommendationDto;
import com.gradlemedium200.productcatalog.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product recommendation operations providing personalized
 * and similarity-based recommendations.
 * 
 * This controller handles the following types of recommendations:
 * - Personalized recommendations based on user history
 * - Similar product recommendations based on product attributes
 * - Trending product recommendations based on overall user activity
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    /**
     * Default limit for recommendation results
     */
    private static final int DEFAULT_RECOMMENDATION_LIMIT = 5;
    
    /**
     * Service for recommendation operations
     */
    private final RecommendationService recommendationService;

    /**
     * Constructor with dependency injection
     * 
     * @param recommendationService service for recommendation operations
     */
    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Get personalized recommendations for a specific user
     * 
     * @param userId the ID of the user to get recommendations for
     * @return ResponseEntity containing personalized recommendations
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<RecommendationDto> getPersonalizedRecommendations(@PathVariable String userId) {
        logger.info("Getting personalized recommendations for user: {}", userId);
        
        try {
            // Validate input
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Invalid user ID provided for personalized recommendations");
                return ResponseEntity.badRequest().build();
            }
            
            // Get recommendations from service with default limit
            RecommendationDto recommendations = 
                recommendationService.getPersonalizedRecommendations(userId, DEFAULT_RECOMMENDATION_LIMIT);
            
            // Check if we got any recommendations
            if (recommendations.getProducts() == null || recommendations.getProducts().isEmpty()) {
                logger.info("No personalized recommendations found for user: {}", userId);
                return ResponseEntity.noContent().build();
            }
            
            logger.info("Returning {} personalized recommendations for user: {}", 
                    recommendations.getRecommendationCount(), userId);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error getting personalized recommendations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get similar products for a specific product
     * 
     * @param productId the ID of the product to find similar items for
     * @return ResponseEntity containing list of similar products
     */
    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<ProductDto>> getSimilarProducts(@PathVariable String productId) {
        logger.info("Getting similar products for product ID: {}", productId);
        
        try {
            // Validate input
            if (productId == null || productId.trim().isEmpty()) {
                logger.warn("Invalid product ID provided for similar products");
                return ResponseEntity.badRequest().build();
            }
            
            // Get similar products from service with default limit
            List<ProductDto> similarProducts = 
                recommendationService.getSimilarProducts(productId, DEFAULT_RECOMMENDATION_LIMIT);
            
            // Check if we got any similar products
            if (similarProducts == null || similarProducts.isEmpty()) {
                logger.info("No similar products found for product ID: {}", productId);
                return ResponseEntity.noContent().build();
            }
            
            logger.info("Returning {} similar products for product ID: {}", similarProducts.size(), productId);
            
            return ResponseEntity.ok(similarProducts);
        } catch (Exception e) {
            logger.error("Error getting similar products for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trending products based on recent user activity
     * 
     * @return ResponseEntity containing list of trending products
     */
    @GetMapping("/trending")
    public ResponseEntity<List<ProductDto>> getTrendingProducts() {
        logger.info("Getting trending products");
        
        try {
            // Get trending products from service with default limit
            List<ProductDto> trendingProducts = 
                recommendationService.getTrendingProducts(DEFAULT_RECOMMENDATION_LIMIT);
            
            // Check if we got any trending products
            if (trendingProducts == null || trendingProducts.isEmpty()) {
                logger.info("No trending products found");
                return ResponseEntity.noContent().build();
            }
            
            logger.info("Returning {} trending products", trendingProducts.size());
            
            return ResponseEntity.ok(trendingProducts);
        } catch (Exception e) {
            logger.error("Error getting trending products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get products frequently bought together with a specific product
     * 
     * @param productId the ID of the product to find frequently bought together products
     * @return ResponseEntity containing list of frequently bought together products
     */
    @GetMapping("/frequently-bought/{productId}")
    public ResponseEntity<List<ProductDto>> getFrequentlyBoughtTogether(@PathVariable String productId) {
        logger.info("Getting frequently bought together products for product ID: {}", productId);
        
        try {
            // Validate input
            if (productId == null || productId.trim().isEmpty()) {
                logger.warn("Invalid product ID provided for frequently bought together");
                return ResponseEntity.badRequest().build();
            }
            
            // Get frequently bought together products from service
            List<ProductDto> frequentlyBoughtProducts = 
                recommendationService.getFrequentlyBoughtTogether(productId);
            
            // Check if we got any products
            if (frequentlyBoughtProducts == null || frequentlyBoughtProducts.isEmpty()) {
                logger.info("No frequently bought together products found for product ID: {}", productId);
                return ResponseEntity.noContent().build();
            }
            
            logger.info("Returning {} frequently bought together products for product ID: {}", 
                    frequentlyBoughtProducts.size(), productId);
            
            return ResponseEntity.ok(frequentlyBoughtProducts);
        } catch (Exception e) {
            logger.error("Error getting frequently bought together products for product {}: {}", 
                    productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get recommendations with custom limit
     * 
     * @param userId the ID of the user to get recommendations for
     * @param limit maximum number of recommendations to return
     * @return ResponseEntity containing personalized recommendations
     */
    @GetMapping("/user/{userId}/limit/{limit}")
    public ResponseEntity<RecommendationDto> getPersonalizedRecommendationsWithLimit(
            @PathVariable String userId, 
            @PathVariable int limit) {
            
        logger.info("Getting personalized recommendations for user: {} with limit: {}", userId, limit);
        
        try {
            // Validate input
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Invalid user ID provided for personalized recommendations");
                return ResponseEntity.badRequest().build();
            }
            
            // Ensure limit is reasonable
            int safeLimit = Math.min(Math.max(limit, 1), 20);
            if (safeLimit != limit) {
                logger.info("Adjusted recommendation limit from {} to {}", limit, safeLimit);
            }
            
            // Get recommendations from service with specified limit
            RecommendationDto recommendations = 
                recommendationService.getPersonalizedRecommendations(userId, safeLimit);
            
            // Check if we got any recommendations
            if (recommendations.getProducts() == null || recommendations.getProducts().isEmpty()) {
                logger.info("No personalized recommendations found for user: {}", userId);
                return ResponseEntity.noContent().build();
            }
            
            logger.info("Returning {} personalized recommendations for user: {}", 
                    recommendations.getRecommendationCount(), userId);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error getting personalized recommendations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // TODO: Add endpoint for multiple product recommendations in a single request
    
    // TODO: Implement caching headers for recommendation responses
    
    // FIXME: Consider adding rate limiting to prevent recommendation API abuse
}