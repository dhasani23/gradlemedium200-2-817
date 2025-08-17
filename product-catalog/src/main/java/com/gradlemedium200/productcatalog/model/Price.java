package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity model representing product pricing information with support for multiple pricing tiers.
 * This model stores various price types (regular, sale, bulk, member) and their validity periods.
 * It also tracks minimum and maximum quantities for tier-based pricing.
 */
@DynamoDBTable(tableName = "Prices")
public class Price {

    /**
     * Unique identifier for the price record
     */
    private String priceId;
    
    /**
     * ID of the product this price applies to
     */
    private String productId;
    
    /**
     * Type of price (REGULAR, SALE, BULK, MEMBER)
     */
    private String priceType;
    
    /**
     * Base price amount
     */
    private BigDecimal basePrice;
    
    /**
     * Sale price if applicable
     */
    private BigDecimal salePrice;
    
    /**
     * Currency code (USD, EUR, etc.)
     */
    private String currency;
    
    /**
     * Price validity start date
     */
    private LocalDateTime validFrom;
    
    /**
     * Price validity end date
     */
    private LocalDateTime validTo;
    
    /**
     * Minimum quantity for this price tier
     */
    private int minQuantity;
    
    /**
     * Maximum quantity for this price tier
     */
    private int maxQuantity;

    /**
     * Default constructor
     */
    public Price() {
        // Default constructor required by DynamoDB
    }

    /**
     * Constructor with all fields
     * 
     * @param priceId Unique identifier for the price record
     * @param productId ID of the product this price applies to
     * @param priceType Type of price (REGULAR, SALE, BULK, MEMBER)
     * @param basePrice Base price amount
     * @param salePrice Sale price if applicable
     * @param currency Currency code
     * @param validFrom Price validity start date
     * @param validTo Price validity end date
     * @param minQuantity Minimum quantity for this price tier
     * @param maxQuantity Maximum quantity for this price tier
     */
    public Price(String priceId, String productId, String priceType, BigDecimal basePrice, 
                BigDecimal salePrice, String currency, LocalDateTime validFrom, 
                LocalDateTime validTo, int minQuantity, int maxQuantity) {
        this.priceId = priceId;
        this.productId = productId;
        this.priceType = priceType;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.currency = currency;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    /**
     * Get price ID
     * 
     * @return the price ID
     */
    @DynamoDBHashKey(attributeName = "priceId")
    public String getPriceId() {
        return priceId;
    }

    /**
     * Set price ID
     * 
     * @param priceId the price ID to set
     */
    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    /**
     * Get product ID
     * 
     * @return the product ID
     */
    @DynamoDBIndexHashKey(attributeName = "productId", globalSecondaryIndexName = "productId-index")
    public String getProductId() {
        return productId;
    }

    /**
     * Set product ID
     * 
     * @param productId the product ID to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get price type
     * 
     * @return the price type
     */
    @DynamoDBAttribute(attributeName = "priceType")
    public String getPriceType() {
        return priceType;
    }

    /**
     * Set price type
     * 
     * @param priceType the price type to set
     */
    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    /**
     * Get base price
     * 
     * @return the base price
     */
    @DynamoDBAttribute(attributeName = "basePrice")
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Set base price
     * 
     * @param basePrice the base price to set
     */
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    /**
     * Get sale price
     * 
     * @return the sale price
     */
    @DynamoDBAttribute(attributeName = "salePrice")
    public BigDecimal getSalePrice() {
        return salePrice;
    }

    /**
     * Set sale price
     * 
     * @param salePrice the sale price to set
     */
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    /**
     * Get currency
     * 
     * @return the currency code
     */
    @DynamoDBAttribute(attributeName = "currency")
    public String getCurrency() {
        return currency;
    }

    /**
     * Set currency
     * 
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Get validity start date
     * 
     * @return the validity start date
     */
    @DynamoDBAttribute(attributeName = "validFrom")
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * Set validity start date
     * 
     * @param validFrom the validity start date to set
     */
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Get validity end date
     * 
     * @return the validity end date
     */
    @DynamoDBAttribute(attributeName = "validTo")
    public LocalDateTime getValidTo() {
        return validTo;
    }

    /**
     * Set validity end date
     * 
     * @param validTo the validity end date to set
     */
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    /**
     * Get minimum quantity for this price tier
     * 
     * @return the minimum quantity
     */
    @DynamoDBAttribute(attributeName = "minQuantity")
    public int getMinQuantity() {
        return minQuantity;
    }

    /**
     * Set minimum quantity for this price tier
     * 
     * @param minQuantity the minimum quantity to set
     */
    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    /**
     * Get maximum quantity for this price tier
     * 
     * @return the maximum quantity
     */
    @DynamoDBAttribute(attributeName = "maxQuantity")
    public int getMaxQuantity() {
        return maxQuantity;
    }

    /**
     * Set maximum quantity for this price tier
     * 
     * @param maxQuantity the maximum quantity to set
     */
    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    /**
     * Get the effective price (sale price if available, otherwise base price)
     * 
     * @return the effective price
     */
    public BigDecimal getEffectivePrice() {
        if (salePrice != null && isOnSale()) {
            return salePrice;
        }
        return basePrice;
    }

    /**
     * Check if price is currently valid based on date range
     * 
     * @return true if price is currently valid, false otherwise
     */
    public boolean isValidPrice() {
        LocalDateTime now = LocalDateTime.now();
        
        // Price is valid if current time is between validFrom and validTo
        // If validFrom is null, consider it valid from the beginning of time
        // If validTo is null, consider it valid until the end of time
        boolean afterStart = (validFrom == null || now.isEqual(validFrom) || now.isAfter(validFrom));
        boolean beforeEnd = (validTo == null || now.isBefore(validTo) || now.isEqual(validTo));
        
        return afterStart && beforeEnd;
    }

    /**
     * Check if product is currently on sale
     * 
     * @return true if product is on sale, false otherwise
     */
    public boolean isOnSale() {
        // Product is on sale if priceType is SALE, sale price is set, and the price is valid
        return "SALE".equals(priceType) 
            && salePrice != null 
            && salePrice.compareTo(basePrice) < 0
            && isValidPrice();
    }

    @Override
    public String toString() {
        return "Price{" +
                "priceId='" + priceId + '\'' +
                ", productId='" + productId + '\'' +
                ", priceType='" + priceType + '\'' +
                ", basePrice=" + basePrice +
                ", salePrice=" + salePrice +
                ", currency='" + currency + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", minQuantity=" + minQuantity +
                ", maxQuantity=" + maxQuantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Price price = (Price) o;
        return priceId != null ? priceId.equals(price.priceId) : price.priceId == null;
    }

    @Override
    public int hashCode() {
        return priceId != null ? priceId.hashCode() : 0;
    }
}