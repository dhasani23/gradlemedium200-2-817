package com.gradlemedium200.productcatalog.service;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.exception.ProductNotFoundException;
import com.gradlemedium200.productcatalog.mapper.ProductMapper;
import com.gradlemedium200.productcatalog.model.Product;
import com.gradlemedium200.productcatalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service layer for product business logic including CRUD operations,
 * validation, and business rules.
 */
@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final ProductMapper productMapper;
    private final Validator validator;
    
    /**
     * Constructor for dependency injection
     * 
     * @param productRepository Repository for product data access
     * @param notificationService Service for sending notifications
     * @param productMapper Mapper for entity-DTO conversion
     */
    @Autowired
    public ProductService(ProductRepository productRepository, 
                         NotificationService notificationService,
                         ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.productMapper = productMapper;
        
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    /**
     * Create a new product with validation
     * 
     * @param productDto the product data transfer object
     * @return the created product as a DTO
     * @throws IllegalArgumentException if validation fails
     */
    public ProductDto createProduct(ProductDto productDto) {
        logger.info("Creating new product: {}", productDto);
        
        // Validate product data
        validateProduct(productDto);
        
        // Generate product ID if not provided
        if (productDto.getProductId() == null || productDto.getProductId().trim().isEmpty()) {
            productDto.setProductId(UUID.randomUUID().toString());
            logger.debug("Generated product ID: {}", productDto.getProductId());
        }
        
        // Convert DTO to entity
        Product product = ProductMapper.toEntity(productDto);
        
        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        // Save product to repository
        Product savedProduct = productRepository.save(product);
        logger.debug("Product saved successfully with ID: {}", savedProduct.getProductId());
        
        // Send notification for new product
        try {
            notificationService.sendProductUpdateNotification(savedProduct.getProductId(), "CREATED");
        } catch (Exception e) {
            // Log error but don't fail the operation
            logger.error("Failed to send product creation notification", e);
            // TODO: Implement retry mechanism for failed notifications
        }
        
        // Convert saved entity back to DTO
        return ProductMapper.toDto(savedProduct);
    }
    
    /**
     * Update existing product
     * 
     * @param productId ID of the product to update
     * @param productDto updated product data
     * @return updated product as a DTO
     * @throws ProductNotFoundException if product doesn't exist
     * @throws IllegalArgumentException if validation fails
     */
    public ProductDto updateProduct(String productId, ProductDto productDto) {
        logger.info("Updating product with ID: {}", productId);
        
        // Validate input parameters
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        // Ensure product ID in DTO matches path parameter
        if (productDto.getProductId() != null && !productDto.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Product ID mismatch between path and request body");
        }
        
        // Set product ID in DTO if not present
        productDto.setProductId(productId);
        
        // Validate product data
        validateProduct(productDto);
        
        // Get existing product
        Product existingProduct = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // Store old price for notification purposes
        BigDecimal oldPrice = existingProduct.getPrice();
        
        // Update existing product with new data
        ProductMapper.updateEntityFromDto(existingProduct, productDto);
        
        // Save updated product
        Product updatedProduct = productRepository.save(existingProduct);
        logger.debug("Product updated successfully: {}", updatedProduct.getProductId());
        
        // Send notifications
        try {
            // Send product update notification
            notificationService.sendProductUpdateNotification(updatedProduct.getProductId(), "UPDATED");
            
            // Send price change notification if price has changed
            if (oldPrice != null && updatedProduct.getPrice() != null 
                    && oldPrice.compareTo(updatedProduct.getPrice()) != 0) {
                notificationService.sendPriceChangeNotification(
                    updatedProduct.getProductId(), 
                    oldPrice, 
                    updatedProduct.getPrice()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the operation
            logger.error("Failed to send product update notifications", e);
        }
        
        // Return updated product as DTO
        return ProductMapper.toDto(updatedProduct);
    }
    
    /**
     * Get product by ID
     * 
     * @param productId ID of the product to retrieve
     * @return the product as a DTO
     * @throws ProductNotFoundException if product doesn't exist
     */
    public ProductDto getProductById(String productId) {
        logger.info("Retrieving product with ID: {}", productId);
        
        // Validate input parameter
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        // Find product by ID
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // Convert entity to DTO and return
        return ProductMapper.toDto(product);
    }
    
    /**
     * Delete product by ID
     * 
     * @param productId ID of the product to delete
     * @throws ProductNotFoundException if product doesn't exist
     */
    public void deleteProduct(String productId) {
        logger.info("Deleting product with ID: {}", productId);
        
        // Validate input parameter
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        
        // Delete product
        productRepository.deleteById(productId);
        logger.debug("Product deleted successfully: {}", productId);
        
        // Send notification for product deletion
        try {
            notificationService.sendProductUpdateNotification(productId, "DELETED");
        } catch (Exception e) {
            // Log error but don't fail the operation
            logger.error("Failed to send product deletion notification", e);
        }
    }
    
    /**
     * Get all products
     * 
     * @return list of all products as DTOs
     */
    public List<ProductDto> getAllProducts() {
        logger.info("Retrieving all products");
        
        // Get all products from repository
        List<Product> products = productRepository.findAll();
        
        // Convert entities to DTOs
        return ProductMapper.toDtoList(products);
    }
    
    /**
     * Get products by category
     * 
     * @param categoryId ID of the category to filter by
     * @return list of products in the specified category
     */
    public List<ProductDto> getProductsByCategory(String categoryId) {
        logger.info("Retrieving products by category ID: {}", categoryId);
        
        // Validate input parameter
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        
        // Find products by category ID
        List<Product> products = productRepository.findByCategoryId(categoryId);
        
        // Convert entities to DTOs
        return ProductMapper.toDtoList(products);
    }
    
    /**
     * Validate product data
     * 
     * @param productDto the product data to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateProduct(ProductDto productDto) {
        logger.debug("Validating product data: {}", productDto);
        
        if (productDto == null) {
            throw new IllegalArgumentException("Product data cannot be null");
        }
        
        // Use Bean Validation API to validate the DTO
        Set<ConstraintViolation<ProductDto>> violations = validator.validate(productDto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Product validation failed: ");
            
            for (ConstraintViolation<ProductDto> violation : violations) {
                sb.append(violation.getPropertyPath())
                  .append(" ")
                  .append(violation.getMessage())
                  .append("; ");
            }
            
            throw new IllegalArgumentException(sb.toString());
        }
        
        // Additional business validation rules
        
        // Validate price is positive
        if (productDto.getPrice() != null && productDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        
        // Validate sale price if present
        if (productDto.getSalePrice() != null) {
            // Sale price must be positive
            if (productDto.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Product sale price must be greater than zero");
            }
            
            // Sale price should be less than or equal to regular price
            if (productDto.getPrice() != null && 
                productDto.getSalePrice().compareTo(productDto.getPrice()) > 0) {
                throw new IllegalArgumentException("Sale price cannot be higher than regular price");
            }
        }
        
        logger.debug("Product validation successful");
    }
    
    /**
     * Check if a product exists by ID
     * 
     * @param productId the product ID to check
     * @return true if product exists, false otherwise
     */
    public boolean productExists(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return false;
        }
        return productRepository.existsById(productId);
    }
    
    // TODO: Add methods for batch operations to improve performance when dealing with multiple products
    
    // TODO: Implement caching mechanism for frequently accessed products
    
    // FIXME: Need to implement proper error handling for database connection failures
}