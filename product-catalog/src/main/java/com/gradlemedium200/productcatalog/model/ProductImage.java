package com.gradlemedium200.productcatalog.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity model representing product images and their metadata stored in S3.
 * Images can be of different types (PRIMARY, THUMBNAIL, GALLERY, ZOOM) and 
 * contain metadata such as dimensions, file size, and upload timestamp.
 */
@DynamoDBTable(tableName = "ProductImages")
public class ProductImage {

    private String imageId;
    private String productId;
    private String imageUrl;
    private String imageType;
    private String altText;
    private int sortOrder;
    private int width;
    private int height;
    private long fileSize;
    private LocalDateTime uploadedAt;

    /**
     * Default constructor required by DynamoDB SDK
     */
    public ProductImage() {
    }

    /**
     * Constructor with all fields
     * 
     * @param imageId Unique identifier for the image
     * @param productId ID of the product this image belongs to
     * @param imageUrl S3 URL of the image
     * @param imageType Type of image (PRIMARY, THUMBNAIL, GALLERY, ZOOM)
     * @param altText Alternative text for accessibility
     * @param sortOrder Display order of the image
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param fileSize Image file size in bytes
     * @param uploadedAt Image upload timestamp
     */
    public ProductImage(String imageId, String productId, String imageUrl, String imageType,
                      String altText, int sortOrder, int width, int height, 
                      long fileSize, LocalDateTime uploadedAt) {
        this.imageId = imageId;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.width = width;
        this.height = height;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Get image ID
     * 
     * @return Unique identifier for the image
     */
    @DynamoDBHashKey(attributeName = "imageId")
    public String getImageId() {
        return imageId;
    }

    /**
     * Set image ID
     * 
     * @param imageId Unique identifier for the image
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * Get product ID
     * 
     * @return ID of the product this image belongs to
     */
    @DynamoDBIndexHashKey(attributeName = "productId", globalSecondaryIndexName = "productId-index")
    public String getProductId() {
        return productId;
    }

    /**
     * Set product ID
     * 
     * @param productId ID of the product this image belongs to
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get image URL
     * 
     * @return S3 URL of the image
     */
    @DynamoDBAttribute(attributeName = "imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Set image URL
     * 
     * @param imageUrl S3 URL of the image
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Get image type
     * 
     * @return Type of image (PRIMARY, THUMBNAIL, GALLERY, ZOOM)
     */
    @DynamoDBAttribute(attributeName = "imageType")
    public String getImageType() {
        return imageType;
    }

    /**
     * Set image type
     * 
     * @param imageType Type of image (PRIMARY, THUMBNAIL, GALLERY, ZOOM)
     */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * Get alternative text
     * 
     * @return Alternative text for accessibility
     */
    @DynamoDBAttribute(attributeName = "altText")
    public String getAltText() {
        return altText;
    }

    /**
     * Set alternative text
     * 
     * @param altText Alternative text for accessibility
     */
    public void setAltText(String altText) {
        this.altText = altText;
    }

    /**
     * Get sort order
     * 
     * @return Display order of the image
     */
    @DynamoDBAttribute(attributeName = "sortOrder")
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Set sort order
     * 
     * @param sortOrder Display order of the image
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Get image width
     * 
     * @return Image width in pixels
     */
    @DynamoDBAttribute(attributeName = "width")
    public int getWidth() {
        return width;
    }

    /**
     * Set image width
     * 
     * @param width Image width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get image height
     * 
     * @return Image height in pixels
     */
    @DynamoDBAttribute(attributeName = "height")
    public int getHeight() {
        return height;
    }

    /**
     * Set image height
     * 
     * @param height Image height in pixels
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get file size
     * 
     * @return Image file size in bytes
     */
    @DynamoDBAttribute(attributeName = "fileSize")
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Set file size
     * 
     * @param fileSize Image file size in bytes
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Get upload timestamp
     * 
     * @return Image upload timestamp
     */
    @DynamoDBAttribute(attributeName = "uploadedAt")
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Set upload timestamp
     * 
     * @param uploadedAt Image upload timestamp
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    /**
     * Check if this is the primary product image
     * 
     * @return true if this is the primary product image, false otherwise
     */
    public boolean isPrimaryImage() {
        return "PRIMARY".equalsIgnoreCase(imageType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductImage that = (ProductImage) o;
        return Objects.equals(imageId, that.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId);
    }

    @Override
    public String toString() {
        return "ProductImage{" +
                "imageId='" + imageId + '\'' +
                ", productId='" + productId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageType='" + imageType + '\'' +
                ", altText='" + altText + '\'' +
                ", sortOrder=" + sortOrder +
                ", width=" + width +
                ", height=" + height +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
    
    // TODO: Add image validation methods to ensure image meets requirements before storing
    
    // TODO: Add method to generate thumbnail URLs from original image URL
    
    // FIXME: LocalDateTime serialization needs converter for DynamoDB
}