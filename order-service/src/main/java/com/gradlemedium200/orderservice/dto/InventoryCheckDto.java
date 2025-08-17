package com.gradlemedium200.orderservice.dto;

/**
 * Data transfer object for inventory check requests.
 * This DTO is used to communicate with the inventory service to check product availability
 * and retrieve inventory information.
 */
public class InventoryCheckDto {

    /**
     * Product identifier to check availability for
     */
    private String productId;

    /**
     * Quantity requested by the customer
     */
    private Integer requestedQuantity;

    /**
     * Available quantity in inventory as reported by inventory service
     */
    private Integer availableQuantity;

    /**
     * Flag indicating whether the requested quantity is available
     * This may be set directly by the inventory service or determined by comparison
     */
    private Boolean isAvailable;

    /**
     * Default constructor
     */
    public InventoryCheckDto() {
    }

    /**
     * Constructor with product ID and requested quantity
     * 
     * @param productId The product identifier
     * @param requestedQuantity The quantity requested
     */
    public InventoryCheckDto(String productId, Integer requestedQuantity) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        // Initially null until set by service or checkAvailability()
        this.isAvailable = null;
    }

    /**
     * Full constructor
     * 
     * @param productId The product identifier
     * @param requestedQuantity The quantity requested
     * @param availableQuantity The available quantity in inventory
     * @param isAvailable Flag indicating availability
     */
    public InventoryCheckDto(String productId, Integer requestedQuantity, Integer availableQuantity, Boolean isAvailable) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.isAvailable = isAvailable;
    }

    /**
     * Checks if requested quantity is available in inventory
     * 
     * @return true if available quantity is sufficient for requested quantity, false otherwise
     */
    public boolean checkAvailability() {
        // If isAvailable is already set, return it
        if (isAvailable != null) {
            return isAvailable;
        }
        
        // Calculate availability based on requested and available quantities
        if (availableQuantity != null && requestedQuantity != null) {
            isAvailable = availableQuantity >= requestedQuantity;
            return isAvailable;
        }
        
        // FIXME: Consider how to handle null values - currently returns false if data is incomplete
        return false;
    }

    /**
     * @return the productId
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * @return the requestedQuantity
     */
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * @param requestedQuantity the requestedQuantity to set
     */
    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    /**
     * @return the availableQuantity
     */
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * @param availableQuantity the availableQuantity to set
     */
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
        // TODO: Consider auto-updating isAvailable when availableQuantity is set
    }

    /**
     * @return the isAvailable flag
     */
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    /**
     * @param isAvailable the isAvailable flag to set
     */
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "InventoryCheckDto{" +
                "productId='" + productId + '\'' +
                ", requestedQuantity=" + requestedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", isAvailable=" + isAvailable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryCheckDto that = (InventoryCheckDto) o;

        if (productId != null ? !productId.equals(that.productId) : that.productId != null) return false;
        if (requestedQuantity != null ? !requestedQuantity.equals(that.requestedQuantity) : that.requestedQuantity != null)
            return false;
        if (availableQuantity != null ? !availableQuantity.equals(that.availableQuantity) : that.availableQuantity != null)
            return false;
        return isAvailable != null ? isAvailable.equals(that.isAvailable) : that.isAvailable == null;
    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (requestedQuantity != null ? requestedQuantity.hashCode() : 0);
        result = 31 * result + (availableQuantity != null ? availableQuantity.hashCode() : 0);
        result = 31 * result + (isAvailable != null ? isAvailable.hashCode() : 0);
        return result;
    }
}