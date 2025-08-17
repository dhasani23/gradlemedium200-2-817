package com.gradlemedium200.productcatalog.service;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.dto.RecommendationDto;
import com.gradlemedium200.productcatalog.model.Product;
import com.gradlemedium200.productcatalog.model.UserActivity;
import com.gradlemedium200.productcatalog.repository.ProductRepository;
import com.gradlemedium200.productcatalog.repository.UserActivityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Service layer for product recommendation algorithms using user activity and product similarity.
 * 
 * This service provides methods for generating different types of product recommendations:
 * - Personalized recommendations based on user's activity history
 * - Similar product recommendations based on product attributes
 * - Trending products based on overall user activity
 * - Frequently bought together recommendations based on purchase patterns
 */
@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    
    // Time windows for user activity analysis
    private static final int RECENT_ACTIVITY_HOURS = 72;  // 3 days
    private static final int TRENDING_WINDOW_HOURS = 48;  // 2 days
    
    // Recommendation types
    private static final String TYPE_PERSONALIZED = "PERSONALIZED";
    private static final String TYPE_SIMILAR = "SIMILAR";
    private static final String TYPE_TRENDING = "TRENDING";
    private static final String TYPE_FREQUENTLY_BOUGHT = "FREQUENTLY_BOUGHT_TOGETHER";
    
    // Activity weighting for recommendation scoring
    private static final double WEIGHT_PURCHASE = 5.0;
    private static final double WEIGHT_CART = 2.5;
    private static final double WEIGHT_VIEW = 1.0;
    private static final double WEIGHT_WISHLIST = 1.5;
    
    private final ProductRepository productRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * Constructor with dependency injection
     * 
     * @param productRepository repository for product data access
     * @param userActivityRepository repository for user activity data
     */
    @Autowired
    public RecommendationService(ProductRepository productRepository, UserActivityRepository userActivityRepository) {
        this.productRepository = productRepository;
        this.userActivityRepository = userActivityRepository;
    }

    /**
     * Get personalized product recommendations for a specific user based on their activity history
     * 
     * @param userId the ID of the user to generate recommendations for
     * @param limit the maximum number of recommendations to return
     * @return a RecommendationDto containing personalized product recommendations
     */
    public RecommendationDto getPersonalizedRecommendations(String userId, int limit) {
        logger.info("Generating personalized recommendations for user: {}, limit: {}", userId, limit);
        
        if (userId == null || userId.isEmpty()) {
            logger.warn("Cannot generate personalized recommendations for null or empty user ID");
            return createEmptyRecommendation(userId, TYPE_PERSONALIZED);
        }
        
        try {
            // Step 1: Get recent user activity
            List<UserActivity> recentActivities = userActivityRepository.findRecentActivities(userId, RECENT_ACTIVITY_HOURS);
            
            if (recentActivities.isEmpty()) {
                logger.info("No recent activity found for user {}, falling back to trending recommendations", userId);
                List<ProductDto> trendingProducts = getTrendingProducts(limit);
                
                RecommendationDto trendingRecs = new RecommendationDto();
                trendingRecs.setProducts(trendingProducts);
                trendingRecs.setRecommendationType(TYPE_TRENDING);
                trendingRecs.setUserId(userId);
                trendingRecs.setReasoning("Based on currently trending products, as we don't have your recent activity");
                return trendingRecs;
            }
            
            // Step 2: Extract product categories and calculate product scores from activity
            Map<String, Double> productScores = calculateProductScores(recentActivities);
            Set<String> userCategories = extractUserCategories(recentActivities);
            
            // Step 3: Find products in the same categories that the user hasn't interacted with
            List<Product> candidateProducts = findCandidateProducts(userCategories, 
                    productScores.keySet().stream().collect(Collectors.toSet()), limit * 3);
                    
            // Step 4: Score candidate products based on similarity to user's interests
            Map<Product, Double> scoredCandidates = scoreCandidateProducts(candidateProducts, userCategories, productScores);
            
            // Step 5: Convert top-scored products to DTOs
            List<ProductDto> recommendedProducts = convertToSortedProductDtos(scoredCandidates, limit);
            
            // Step 6: Create and return the recommendation DTO
            String reasoning = generatePersonalizedReasoning(userCategories);
            
            RecommendationDto recommendation = new RecommendationDto();
            recommendation.setUserId(userId);
            recommendation.setRecommendationType(TYPE_PERSONALIZED);
            recommendation.setProducts(recommendedProducts);
            recommendation.setReasoning(reasoning);
            recommendation.setConfidence(calculateConfidenceScore(recentActivities.size(), recommendedProducts.size()));
            recommendation.setGeneratedAt(LocalDateTime.now());
            
            logger.info("Generated {} personalized recommendations for user {}", 
                    recommendation.getProducts().size(), userId);
            
            return recommendation;
        } catch (Exception e) {
            logger.error("Error generating personalized recommendations for user {}: {}", userId, e.getMessage(), e);
            return createEmptyRecommendation(userId, TYPE_PERSONALIZED);
        }
    }

    /**
     * Get products similar to a given product based on product attributes
     * 
     * @param productId the ID of the product to find similar items for
     * @param limit the maximum number of similar products to return
     * @return a list of product DTOs similar to the specified product
     */
    public List<ProductDto> getSimilarProducts(String productId, int limit) {
        logger.info("Finding similar products for product ID: {}, limit: {}", productId, limit);
        
        if (productId == null || productId.isEmpty()) {
            logger.warn("Cannot find similar products for null or empty product ID");
            return Collections.emptyList();
        }
        
        try {
            // Step 1: Get the source product
            Optional<Product> sourceProductOpt = productRepository.findById(productId);
            
            if (!sourceProductOpt.isPresent()) {
                logger.warn("Product not found with ID: {}", productId);
                return Collections.emptyList();
            }
            
            Product sourceProduct = sourceProductOpt.get();
            
            // Step 2: Find products in the same category
            List<Product> sameCategory = productRepository.findByCategoryId(sourceProduct.getCategoryId());
            
            // Step 3: Filter out the source product itself
            sameCategory = sameCategory.stream()
                .filter(p -> !p.getProductId().equals(productId))
                .collect(Collectors.toList());
            
            // Step 4: Calculate similarity scores
            Map<Product, Double> similarityScores = new HashMap<>();
            for (Product candidate : sameCategory) {
                double score = calculateSimilarityScore(sourceProduct, candidate);
                similarityScores.put(candidate, score);
            }
            
            // Step 5: Sort by similarity score and convert to DTOs
            return similarityScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> convertToProductDto(entry.getKey()))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error finding similar products for product {}: {}", productId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get trending products based on recent user activity across all users
     * 
     * @param limit the maximum number of trending products to return
     * @return a list of trending product DTOs
     */
    public List<ProductDto> getTrendingProducts(int limit) {
        logger.info("Finding trending products with limit: {}", limit);
        
        try {
            // Step 1: Get recent activities across all users
            // Note: In a real implementation, this would likely use a more efficient
            // query method or analytics service rather than fetching all activities
            List<UserActivity> recentActivities = userActivityRepository.findByMultipleCriteria(
                null, null, null, 
                LocalDateTime.now().minusHours(TRENDING_WINDOW_HOURS), 
                LocalDateTime.now());
            
            if (recentActivities.isEmpty()) {
                logger.info("No recent activity found, returning empty trending list");
                return Collections.emptyList();
            }
            
            // Step 2: Count and weight product interactions
            Map<String, Double> productScores = new HashMap<>();
            
            for (UserActivity activity : recentActivities) {
                String productId = activity.getProductId();
                if (productId == null) continue;
                
                double weight = getActivityWeight(activity.getActivityType());
                productScores.put(productId, productScores.getOrDefault(productId, 0.0) + weight);
            }
            
            // Step 3: Sort products by score and get top products
            List<String> topProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            // Step 4: Fetch product details and convert to DTOs
            List<ProductDto> trendingProducts = new ArrayList<>();
            for (String productId : topProductIds) {
                productRepository.findById(productId).ifPresent(
                    product -> trendingProducts.add(convertToProductDto(product))
                );
            }
            
            logger.info("Found {} trending products", trendingProducts.size());
            return trendingProducts;
            
        } catch (Exception e) {
            logger.error("Error finding trending products: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get products frequently bought together with a specific product
     * based on purchase history analysis
     * 
     * @param productId the ID of the product to find frequently bought together items for
     * @return a list of product DTOs frequently bought with the specified product
     */
    public List<ProductDto> getFrequentlyBoughtTogether(String productId) {
        logger.info("Finding frequently bought together products for product ID: {}", productId);
        
        if (productId == null || productId.isEmpty()) {
            logger.warn("Cannot find frequently bought together products for null or empty product ID");
            return Collections.emptyList();
        }
        
        try {
            // Step 1: Find all purchase activities involving the specified product
            List<UserActivity> purchaseActivities = userActivityRepository.findByMultipleCriteria(
                null, productId, UserActivity.ACTIVITY_TYPE_PURCHASE, null, null);
            
            if (purchaseActivities.isEmpty()) {
                logger.info("No purchase history found for product {}", productId);
                return Collections.emptyList();
            }
            
            // Step 2: Extract user IDs who purchased this product
            Set<String> userIds = purchaseActivities.stream()
                .map(UserActivity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            // Step 3: Find other products these users purchased in the same time window
            Map<String, Integer> coPurchaseCounts = new HashMap<>();
            
            for (String userId : userIds) {
                // Get all purchase activities for this user
                List<UserActivity> userPurchases = userActivityRepository.findByMultipleCriteria(
                    userId, null, UserActivity.ACTIVITY_TYPE_PURCHASE, null, null);
                
                // Group purchases by session to identify products bought together
                Map<String, List<String>> sessionPurchases = new HashMap<>();
                
                for (UserActivity purchase : userPurchases) {
                    if (purchase.getSessionId() == null || purchase.getProductId() == null) continue;
                    
                    sessionPurchases
                        .computeIfAbsent(purchase.getSessionId(), k -> new ArrayList<>())
                        .add(purchase.getProductId());
                }
                
                // Count co-purchases in the same session
                for (List<String> products : sessionPurchases.values()) {
                    if (products.contains(productId)) {
                        for (String otherProductId : products) {
                            if (!otherProductId.equals(productId)) {
                                coPurchaseCounts.put(otherProductId, 
                                    coPurchaseCounts.getOrDefault(otherProductId, 0) + 1);
                            }
                        }
                    }
                }
            }
            
            // Step 4: Sort by co-purchase count and fetch product details
            List<String> topCoPurchasedIds = coPurchaseCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            // Step 5: Convert to DTOs
            List<ProductDto> frequentlyBoughtTogether = new ArrayList<>();
            for (String coProductId : topCoPurchasedIds) {
                productRepository.findById(coProductId).ifPresent(
                    product -> frequentlyBoughtTogether.add(convertToProductDto(product))
                );
            }
            
            logger.info("Found {} products frequently bought with product {}", 
                    frequentlyBoughtTogether.size(), productId);
            
            return frequentlyBoughtTogether;
            
        } catch (Exception e) {
            logger.error("Error finding frequently bought together products for {}: {}", 
                productId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculate similarity score between two products
     * 
     * @param source source product
     * @param candidate candidate product for similarity comparison
     * @return similarity score between 0 and 1
     */
    private double calculateSimilarityScore(Product source, Product candidate) {
        double score = 0.0;
        int factors = 0;
        
        // Same category is already a given, but we can check subcategory if available
        if (Objects.equals(source.getCategoryId(), candidate.getCategoryId())) {
            score += 0.5;
            factors++;
        }
        
        // Compare attributes like brand
        if (source.getBrand() != null && source.getBrand().equals(candidate.getBrand())) {
            score += 1.0;
            factors++;
        }
        
        // Compare price range
        if (source.getPrice() != null && candidate.getPrice() != null) {
            double sourcePriceValue = source.getPrice().doubleValue();
            double candidatePriceValue = candidate.getPrice().doubleValue();
            
            // Check if candidate price is within 20% of source price
            double priceDiffRatio = Math.abs(sourcePriceValue - candidatePriceValue) / sourcePriceValue;
            if (priceDiffRatio <= 0.2) {
                score += 1.0;
                factors++;
            }
        }
        
        // Compare tags (if available)
        if (source.getTags() != null && candidate.getTags() != null && 
            !source.getTags().isEmpty() && !candidate.getTags().isEmpty()) {
            
            Set<String> sourceTags = new HashSet<>(source.getTags());
            Set<String> candidateTags = new HashSet<>(candidate.getTags());
            
            // Calculate Jaccard similarity for tags
            Set<String> intersection = new HashSet<>(sourceTags);
            intersection.retainAll(candidateTags);
            
            Set<String> union = new HashSet<>(sourceTags);
            union.addAll(candidateTags);
            
            double tagSimilarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            score += tagSimilarity * 2.0; // Weight tag similarity more heavily
            factors++;
        }
        
        // Normalize score based on number of factors considered
        return factors > 0 ? score / factors : 0;
    }
    
    /**
     * Calculate product scores based on user activities
     * 
     * @param activities list of user activities
     * @return map of product IDs to their calculated scores
     */
    private Map<String, Double> calculateProductScores(List<UserActivity> activities) {
        Map<String, Double> scores = new HashMap<>();
        
        for (UserActivity activity : activities) {
            String productId = activity.getProductId();
            if (productId == null) continue;
            
            double weight = getActivityWeight(activity.getActivityType());
            
            // Apply recency boost - more recent activities get higher weight
            LocalDateTime now = LocalDateTime.now();
            if (activity.getTimestamp() != null) {
                long hoursAgo = java.time.Duration.between(activity.getTimestamp(), now).toHours();
                double recencyFactor = Math.max(0.5, 1.0 - (hoursAgo / (double) RECENT_ACTIVITY_HOURS));
                weight *= recencyFactor;
            }
            
            scores.put(productId, scores.getOrDefault(productId, 0.0) + weight);
        }
        
        return scores;
    }
    
    /**
     * Extract categories from user activity
     * 
     * @param activities list of user activities
     * @return set of category IDs the user has interacted with
     */
    private Set<String> extractUserCategories(List<UserActivity> activities) {
        return activities.stream()
            .map(UserActivity::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    /**
     * Find candidate products for recommendations based on user's category interests
     * 
     * @param categories categories the user is interested in
     * @param excludeProductIds products to exclude (already interacted with)
     * @param limit maximum number of candidate products to return
     * @return list of candidate products
     */
    private List<Product> findCandidateProducts(Set<String> categories, Set<String> excludeProductIds, int limit) {
        // This is a simplified implementation that could be optimized in a real system
        List<Product> candidates = new ArrayList<>();
        
        // For each category, find products
        for (String categoryId : categories) {
            List<Product> categoryProducts = productRepository.findByCategoryId(categoryId);
            
            // Filter out products the user has already interacted with
            categoryProducts = categoryProducts.stream()
                .filter(p -> !excludeProductIds.contains(p.getProductId()))
                .collect(Collectors.toList());
                
            candidates.addAll(categoryProducts);
            
            if (candidates.size() >= limit * 2) {
                break;  // We have enough candidates
            }
        }
        
        return candidates.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Score candidate products based on user interests
     * 
     * @param candidates list of candidate products
     * @param userCategories categories the user is interested in
     * @param productScores scores of products the user has interacted with
     * @return map of products to their calculated recommendation scores
     */
    private Map<Product, Double> scoreCandidateProducts(
            List<Product> candidates, 
            Set<String> userCategories,
            Map<String, Double> productScores) {
        
        Map<Product, Double> scores = new HashMap<>();
        
        for (Product candidate : candidates) {
            double score = 0.0;
            
            // Category match score
            if (userCategories.contains(candidate.getCategoryId())) {
                score += 1.0;
            }
            
            // Find similar products the user has interacted with
            for (Map.Entry<String, Double> entry : productScores.entrySet()) {
                productRepository.findById(entry.getKey()).ifPresent(interactedProduct -> {
                    double similarity = calculateSimilarityScore(interactedProduct, candidate);
                    double userScore = entry.getValue();
                    
                    // Product score is weighted by similarity and user's interest
                    scores.put(candidate, scores.getOrDefault(candidate, 0.0) + 
                        similarity * userScore * 0.3);
                });
            }
            
            // Base score just for being in the right category
            scores.putIfAbsent(candidate, score);
        }
        
        return scores;
    }
    
    /**
     * Convert products with scores to sorted ProductDto list
     * 
     * @param productScores map of products and their scores
     * @param limit maximum number of products to include
     * @return sorted list of product DTOs
     */
    private List<ProductDto> convertToSortedProductDtos(Map<Product, Double> productScores, int limit) {
        return productScores.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .map(entry -> convertToProductDto(entry.getKey()))
            .collect(Collectors.toList());
    }
    
    /**
     * Generate reasoning for personalized recommendations
     * 
     * @param categories categories the user is interested in
     * @return explanation string for the recommendations
     */
    private String generatePersonalizedReasoning(Set<String> categories) {
        if (categories.isEmpty()) {
            return "Based on your recent activity";
        }
        
        return "Based on your interest in " + 
            String.join(", ", categories) + 
            " and your recent product interactions";
    }
    
    /**
     * Calculate confidence score for recommendations
     * 
     * @param activityCount number of user activities used
     * @param recommendationCount number of recommendations generated
     * @return confidence score between 0 and 1
     */
    private double calculateConfidenceScore(int activityCount, int recommendationCount) {
        // More activity data and more recommendations = higher confidence
        double activityFactor = Math.min(1.0, activityCount / 10.0);
        double recommendationFactor = Math.min(1.0, recommendationCount / 5.0);
        
        return (activityFactor * 0.7) + (recommendationFactor * 0.3);
    }
    
    /**
     * Create empty recommendation object when recommendations cannot be generated
     * 
     * @param userId user ID
     * @param type recommendation type
     * @return empty recommendation DTO
     */
    private RecommendationDto createEmptyRecommendation(String userId, String type) {
        RecommendationDto recommendation = new RecommendationDto();
        recommendation.setUserId(userId);
        recommendation.setRecommendationType(type);
        recommendation.setProducts(Collections.emptyList());
        recommendation.setReasoning("Not enough data to generate recommendations");
        recommendation.setConfidence(0.0);
        recommendation.setGeneratedAt(LocalDateTime.now());
        return recommendation;
    }
    
    /**
     * Convert a Product entity to a ProductDto
     * 
     * @param product the product entity to convert
     * @return product DTO
     */
    private ProductDto convertToProductDto(Product product) {
        // In a real implementation, this would likely use a mapper class
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        
        if (product.getImageUrls() != null) {
            dto.setImageUrls(new ArrayList<>(product.getImageUrls()));
        }
        
        if (product.getTags() != null) {
            dto.setTags(new HashSet<>(product.getTags()));
        }
        
        // Additional fields could be mapped here as needed
        
        return dto;
    }
    
    /**
     * Get weight factor for different activity types
     * 
     * @param activityType type of user activity
     * @return weight factor for the activity type
     */
    private double getActivityWeight(String activityType) {
        if (activityType == null) {
            return 0.0;
        }
        
        switch (activityType) {
            case UserActivity.ACTIVITY_TYPE_PURCHASE:
                return WEIGHT_PURCHASE;
            case UserActivity.ACTIVITY_TYPE_ADD_TO_CART:
                return WEIGHT_CART;
            case UserActivity.ACTIVITY_TYPE_WISHLIST:
                return WEIGHT_WISHLIST;
            case UserActivity.ACTIVITY_TYPE_VIEW:
                return WEIGHT_VIEW;
            default:
                return 0.5;
        }
    }
    
    // TODO: Implement caching for frequently requested recommendations
    
    // TODO: Add A/B testing capabilities to measure effectiveness of different recommendation algorithms
    
    // FIXME: The current implementation loads all data in memory which isn't scalable.
    // Consider using more efficient queries or a specialized recommendation engine for large datasets.
}