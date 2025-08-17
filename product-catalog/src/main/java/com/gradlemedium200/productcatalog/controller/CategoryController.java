package com.gradlemedium200.productcatalog.controller;

import com.gradlemedium200.productcatalog.dto.CategoryDto;
import com.gradlemedium200.productcatalog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

/**
 * REST controller for category management operations providing 
 * category hierarchy and CRUD endpoints.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    private static final Logger logger = Logger.getLogger(CategoryController.class.getName());
    
    private final CategoryService categoryService;
    
    /**
     * Constructor injection of CategoryService
     *
     * @param categoryService Service for category operations
     */
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    /**
     * Create a new category
     *
     * @param categoryDto The category data to create
     * @return ResponseEntity containing the created category data
     */
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        logger.info("Creating new category: " + categoryDto.getName());
        
        try {
            CategoryDto createdCategory = categoryService.createCategory(categoryDto);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to create category: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.severe("Unexpected error creating category: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get a category by ID
     *
     * @param categoryId The ID of the category to retrieve
     * @return ResponseEntity containing the category data if found
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable String categoryId) {
        logger.info("Fetching category with ID: " + categoryId);
        
        try {
            CategoryDto category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            logger.warning("Category not found with ID: " + categoryId);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all categories as a flat list
     *
     * @return ResponseEntity containing a list of all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        logger.info("Fetching all categories");
        
        try {
            List<CategoryDto> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.severe("Error fetching all categories: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get the complete category hierarchy as a nested tree structure
     *
     * @return ResponseEntity containing the category hierarchy
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<List<CategoryDto>> getCategoryHierarchy() {
        logger.info("Fetching category hierarchy");
        
        try {
            List<CategoryDto> categoryHierarchy = categoryService.getCategoryHierarchy();
            return ResponseEntity.ok(categoryHierarchy);
        } catch (Exception e) {
            logger.severe("Error fetching category hierarchy: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update an existing category
     *
     * @param categoryId The ID of the category to update
     * @param categoryDto The updated category data
     * @return ResponseEntity containing the updated category data
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryDto categoryDto) {
        logger.info("Updating category with ID: " + categoryId);
        
        try {
            // Check if the ID in the path matches the DTO if present
            if (categoryDto.getCategoryId() != null && !categoryDto.getCategoryId().equals(categoryId)) {
                logger.warning("Category ID mismatch between path and request body");
                return ResponseEntity.badRequest().build();
            }
            
            // Ensure the category ID is set correctly
            categoryDto.setCategoryId(categoryId);
            
            CategoryDto updatedCategory = categoryService.updateCategory(categoryId, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid request to update category: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.warning("Category not found or error updating: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get subcategories of a parent category
     *
     * @param categoryId The parent category ID
     * @return ResponseEntity containing a list of subcategories
     */
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<CategoryDto>> getSubcategories(@PathVariable String categoryId) {
        logger.info("Fetching subcategories for parent ID: " + categoryId);
        
        try {
            List<CategoryDto> subcategories = categoryService.getSubcategories(categoryId);
            return ResponseEntity.ok(subcategories);
        } catch (Exception e) {
            logger.warning("Error fetching subcategories: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a category (endpoint not implemented yet)
     *
     * @param categoryId The ID of the category to delete
     * @return ResponseEntity with status code
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        // TODO: Implement category deletion - should consider moving child categories or preventing deletion
        logger.warning("Category deletion not implemented yet");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}