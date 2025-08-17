package com.gradlemedium200.productcatalog.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.gradlemedium200.productcatalog.config.AwsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing product images and S3 storage operations.
 * This service handles uploading, deleting and processing of product images.
 */
@Service
public class ImageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;
    
    // Thumbnail dimensions
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    
    // Image formats
    private static final String PNG_FORMAT = "png";
    private static final String JPEG_FORMAT = "jpeg";
    
    /**
     * Constructs the ImageService with required dependencies.
     * 
     * @param amazonS3 Amazon S3 client for image operations
     * @param awsConfig AWS configuration containing bucket information
     */
    @Autowired
    public ImageService(AmazonS3 amazonS3, AwsConfig awsConfig) {
        this.amazonS3 = amazonS3;
        this.bucketName = awsConfig.getS3BucketName();
    }

    /**
     * Uploads a product image to S3 and returns the URL of the uploaded image.
     * 
     * @param productId The product ID associated with the image
     * @param imageData The binary image data
     * @param contentType The content type of the image (e.g. "image/jpeg", "image/png")
     * @return URL of the uploaded image
     * @throws IOException if there's an error reading or uploading the image
     */
    public String uploadImage(String productId, byte[] imageData, String contentType) throws IOException {
        // Input validation
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        
        // Generate a unique key for the image
        String imageKey = generateImageKey(productId);
        
        // Setup metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageData.length);
        metadata.setContentType(contentType);
        
        try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
            // Create upload request with public read access
            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    imageKey,
                    inputStream,
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);
            
            // Upload to S3
            amazonS3.putObject(request);
            
            // Return the public URL
            return amazonS3.getUrl(bucketName, imageKey).toString();
        } catch (Exception e) {
            // TODO: Add proper logging and error handling
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes an image from S3 based on the image URL.
     * 
     * @param imageUrl The URL of the image to delete
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        
        try {
            // Extract the key from URL
            String imageKey = extractKeyFromUrl(imageUrl);
            
            // Check if the image exists
            if (amazonS3.doesObjectExist(bucketName, imageKey)) {
                amazonS3.deleteObject(bucketName, imageKey);
            } else {
                // FIXME: Use proper logging framework instead of System.out
                System.out.println("Image does not exist in S3: " + imageKey);
            }
        } catch (Exception e) {
            // TODO: Add proper error handling and logging
            throw new RuntimeException("Failed to delete image from S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generates a thumbnail for an existing product image and uploads it to S3.
     * 
     * @param originalImageUrl URL of the original image
     * @return URL of the generated thumbnail
     * @throws IOException if there's an error processing or uploading the thumbnail
     */
    public String generateThumbnail(String originalImageUrl) throws IOException {
        if (originalImageUrl == null || originalImageUrl.isEmpty()) {
            throw new IllegalArgumentException("Original image URL cannot be null or empty");
        }
        
        try {
            // Extract key from the original URL
            String originalKey = extractKeyFromUrl(originalImageUrl);
            
            // Get the object from S3
            S3Object s3Object = amazonS3.getObject(bucketName, originalKey);
            
            // Read image data
            byte[] imageData = IOUtils.toByteArray(s3Object.getObjectContent());
            
            // Generate thumbnail
            byte[] thumbnailData = resizeImage(imageData, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            
            // Generate thumbnail key
            String thumbnailKey = originalKey.replaceFirst("images/", "images/thumbnails/");
            
            // Upload thumbnail
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(thumbnailData.length);
            metadata.setContentType(s3Object.getObjectMetadata().getContentType());
            
            try (InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailData)) {
                PutObjectRequest request = new PutObjectRequest(
                        bucketName,
                        thumbnailKey,
                        thumbnailInputStream,
                        metadata
                ).withCannedAcl(CannedAccessControlList.PublicRead);
                
                amazonS3.putObject(request);
                
                return amazonS3.getUrl(bucketName, thumbnailKey).toString();
            }
        } catch (Exception e) {
            // TODO: Add proper logging and error handling
            throw new IOException("Failed to generate thumbnail: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves metadata for an image stored in S3.
     * 
     * @param imageUrl URL of the image
     * @return Map containing image metadata
     */
    public Map<String, Object> getImageMetadata(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            String imageKey = extractKeyFromUrl(imageUrl);
            
            if (!amazonS3.doesObjectExist(bucketName, imageKey)) {
                throw new IllegalArgumentException("Image does not exist: " + imageUrl);
            }
            
            // Get object metadata from S3
            ObjectMetadata s3Metadata = amazonS3.getObjectMetadata(bucketName, imageKey);
            
            // Extract basic metadata
            metadata.put("contentType", s3Metadata.getContentType());
            metadata.put("contentLength", s3Metadata.getContentLength());
            metadata.put("lastModified", s3Metadata.getLastModified());
            metadata.put("eTag", s3Metadata.getETag());
            
            // Add any user metadata
            metadata.put("userMetadata", s3Metadata.getUserMetadata());
            
            // Add image dimensions if possible
            try {
                // Get image for dimension analysis
                S3Object s3Object = amazonS3.getObject(bucketName, imageKey);
                BufferedImage image = ImageIO.read(s3Object.getObjectContent());
                
                if (image != null) {
                    metadata.put("width", image.getWidth());
                    metadata.put("height", image.getHeight());
                }
            } catch (Exception e) {
                // Ignore image dimension errors, but still provide other metadata
                // TODO: Add proper logging
                System.out.println("Could not determine image dimensions: " + e.getMessage());
            }
            
            return metadata;
        } catch (Exception e) {
            // TODO: Add proper error handling and logging
            throw new RuntimeException("Failed to get image metadata: " + e.getMessage(), e);
        }
    }
    
    /**
     * Resizes an image to the specified dimensions.
     * 
     * @param imageData Binary image data
     * @param width Target width
     * @param height Target height
     * @return Resized image as byte array
     * @throws IOException if there's an error processing the image
     */
    private byte[] resizeImage(byte[] imageData, int width, int height) throws IOException {
        // Read original image
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        if (originalImage == null) {
            throw new IOException("Cannot read image data");
        }
        
        // Determine image type
        String formatName = determineImageFormat(originalImage);
        
        // Create new resized image
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Draw the resized image
        Graphics2D g = resizedImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g.drawImage(originalImage, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        
        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, formatName, outputStream);
        
        return outputStream.toByteArray();
    }
    
    /**
     * Determines the format of an image.
     * 
     * @param image BufferedImage to analyze
     * @return Format name ("jpeg" or "png")
     */
    private String determineImageFormat(BufferedImage image) {
        // Default to JPEG
        return image.getColorModel().hasAlpha() ? PNG_FORMAT : JPEG_FORMAT;
    }
    
    /**
     * Generates a unique key for storing an image in S3.
     * 
     * @param productId Product ID associated with the image
     * @return Unique S3 object key
     */
    private String generateImageKey(String productId) {
        String uuid = UUID.randomUUID().toString();
        return String.format("images/%s/%s", productId, uuid);
    }
    
    /**
     * Extracts the S3 object key from an image URL.
     * 
     * @param imageUrl Full image URL
     * @return S3 object key
     */
    private String extractKeyFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            
            // Remove leading slash if present
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            // If the path contains the bucket name, remove it
            if (path.startsWith(bucketName + "/")) {
                path = path.substring(bucketName.length() + 1);
            }
            
            return path;
        } catch (Exception e) {
            // TODO: Add proper error handling
            throw new IllegalArgumentException("Invalid image URL: " + imageUrl, e);
        }
    }
    
    // FIXME: Add support for handling various image formats and validations
    // TODO: Implement image caching mechanism for better performance
    // TODO: Add support for image watermarking and other transformations
}