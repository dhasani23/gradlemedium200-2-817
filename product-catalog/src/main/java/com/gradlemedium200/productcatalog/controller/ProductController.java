package com.gradlemedium200.productcatalog.controller;

import com.gradlemedium200.productcatalog.dto.ProductDto;
import com.gradlemedium200.productcatalog.exception.ProductNotFoundException;
import com.gradlemedium200.productcatalog.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for product management operations providing comprehensive product API endpoints.
 * Handles all HTTP requests related to product management including CRUD operations, 
 * and product listing by category.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;

    /**
     * Constructor for dependency injection
     * 
     * @param productService Service for product operations
     */
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Create a new product
     * 
     * @param productDto Product data transfer object containing product information
     * @return ResponseEntity containing the created product and HTTP status 201 CREATED
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        logger.info("REST request to create new product: {}", productDto);
        
        try {
            ProductDto createdProduct = productService.createProduct(productDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create product due to validation error", e);
            throw e; // Will be handled by global exception handler
        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating product", e);
            throw e; // Will be handled by global exception handler
        }
    }

    /**
     * Get product by ID
     * 
     * @param productId The ID of the product to retrieve
     * @return ResponseEntity containing the product and HTTP status 200 OK
     * @throws ProductNotFoundException if product does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("id") String productId) {
        logger.info("REST request to get product with ID: {}", productId);
        
        try {
            ProductDto product = productService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.error("Product not found with ID: {}", productId);
            throw e; // Will be handled by global exception handler
        }
    }

    /**
     * Update an existing product
     * 
     * @param productId The ID of the product to update
     * @param productDto Product data transfer object containing updated information
     * @return ResponseEntity containing the updated product and HTTP status 200 OK
     * @throws ProductNotFoundException if product does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable("id") String productId, 
                                                 @Valid @RequestBody ProductDto productDto) {
        logger.info("REST request to update product with ID: {}", productId);
        
        try {
            ProductDto updatedProduct = productService.updateProduct(productId, productDto);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException e) {
            logger.error("Product not found with ID: {}", productId);
            throw e; // Will be handled by global exception handler
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update product due to validation error", e);
            throw e; // Will be handled by global exception handler
        }
    }

    /**
     * Delete a product by ID
     * 
     * @param productId The ID of the product to delete
     * @return ResponseEntity with HTTP status 204 NO CONTENT
     * @throws ProductNotFoundException if product does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") String productId) {
        logger.info("REST request to delete product with ID: {}", productId);
        
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            logger.error("Cannot delete non-existent product with ID: {}", productId);
            throw e; // Will be handled by global exception handler
        }
    }

    /**
     * Get all products
     * 
     * @return ResponseEntity containing a list of all products and HTTP status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        logger.info("REST request to get all products");
        
        List<ProductDto> products = productService.getAllProducts();
        logger.debug("Retrieved {} products", products.size());
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category ID
     * 
     * @param categoryId The ID of the category to filter by
     * @return ResponseEntity containing a list of products in the specified category and HTTP status 200 OK
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String categoryId) {
        logger.info("REST request to get products by category ID: {}", categoryId);
        
        try {
            List<ProductDto> products = productService.getProductsByCategory(categoryId);
            logger.debug("Retrieved {} products for category {}", products.size(), categoryId);
            
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category ID: {}", categoryId);
            throw e; // Will be handled by global exception handler
        }
    }
    
    // TODO: Add endpoint for batch product operations to improve performance for bulk updates
    
    // TODO: Consider adding pagination support for product listing endpoints
    
    // TODO: Implement filtering and sorting options for getAllProducts endpoint
    
    // FIXME: Add proper CORS configuration to allow access from frontend applications
}