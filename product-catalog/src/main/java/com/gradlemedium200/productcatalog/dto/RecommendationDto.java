package com.gradlemedium200.productcatalog.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for product recommendations with reasoning.
 * This DTO encapsulates recommendation data including the list of recommended products,
 * recommendation type, confidence score, and the reasoning behind the recommendations.
 */
public class RecommendationDto {

    /**
     * User ID for personalized recommendations
     */
    private String userId;
    
    /**
     * Type of recommendation
     */
    private String recommendationType;
    
    /**
     * List of recommended products
     */
    private List<ProductDto> products;
    
    /**
     * Explanation of why products were recommended
     */
    private String reasoning;
    
    /**
     * Confidence score of recommendations (0-1)
     */
    private double confidence;
    
    /**
     * When recommendations were generated
     */
    private LocalDateTime generatedAt;

    /**
     * Default constructor
     */
    public RecommendationDto() {
        this.products = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with essential fields
     * 
     * @param userId user identifier for personalization
     * @param recommendationType type of recommendation
     */
    public RecommendationDto(String userId, String recommendationType) {
        this();
        this.userId = userId;
        this.recommendationType = recommendationType;
    }

    /**
     * Full constructor
     * 
     * @param userId user identifier
     * @param recommendationType type of recommendation
     * @param products list of recommended products
     * @param reasoning explanation for recommendations
     * @param confidence confidence score
     */
    public RecommendationDto(String userId, String recommendationType, List<ProductDto> products, 
                            String reasoning, double confidence) {
        this.userId = userId;
        this.recommendationType = recommendationType;
        this.products = products != null ? products : new ArrayList<>();
        this.reasoning = reasoning;
        this.confidence = confidence;
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Get user ID
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID
     * 
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get recommendation type
     * 
     * @return the recommendation type
     */
    public String getRecommendationType() {
        return recommendationType;
    }

    /**
     * Set recommendation type
     * 
     * @param recommendationType the recommendation type to set
     */
    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    /**
     * Get recommended products
     * 
     * @return list of recommended products
     */
    public List<ProductDto> getProducts() {
        return products;
    }

    /**
     * Set recommended products
     * 
     * @param products list of products to set
     */
    public void setProducts(List<ProductDto> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    /**
     * Add a product to recommendations
     * 
     * @param product product to add to recommendations
     */
    public void addProduct(ProductDto product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        if (product != null) {
            this.products.add(product);
        }
    }

    /**
     * Get reasoning for recommendations
     * 
     * @return explanation for recommendations
     */
    public String getReasoning() {
        return reasoning;
    }

    /**
     * Set reasoning for recommendations
     * 
     * @param reasoning explanation to set
     */
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    /**
     * Get confidence score
     * 
     * @return confidence score between 0 and 1
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Set confidence score
     * 
     * @param confidence confidence score to set
     */
    public void setConfidence(double confidence) {
        // Ensure confidence is between 0 and 1
        if (confidence < 0) {
            this.confidence = 0;
        } else if (confidence > 1) {
            this.confidence = 1;
        } else {
            this.confidence = confidence;
        }
    }

    /**
     * Get generation timestamp
     * 
     * @return when recommendations were generated
     */
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    /**
     * Set generation timestamp
     * 
     * @param generatedAt timestamp to set
     */
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * Check if recommendations have high confidence (>0.7)
     * 
     * @return true if confidence score is greater than 0.7
     */
    public boolean isHighConfidence() {
        return confidence > 0.7;
    }

    /**
     * Get number of recommended products
     * 
     * @return count of recommended products
     */
    public int getRecommendationCount() {
        return products != null ? products.size() : 0;
    }

    /**
     * Returns string representation of the recommendation
     */
    @Override
    public String toString() {
        return "RecommendationDto{" +
                "userId='" + userId + '\'' +
                ", recommendationType='" + recommendationType + '\'' +
                ", products=" + (products != null ? products.size() : 0) + " items" +
                ", reasoning='" + (reasoning != null ? reasoning.substring(0, Math.min(reasoning.length(), 20)) + "..." : "null") + '\'' +
                ", confidence=" + confidence +
                ", generatedAt=" + generatedAt +
                '}';
    }

    // TODO: Add equals and hashCode methods

    // FIXME: Consider adding validation for recommendationType to ensure it's one of the valid types
}