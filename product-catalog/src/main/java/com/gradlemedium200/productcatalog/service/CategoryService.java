package com.gradlemedium200.productcatalog.service;

import com.gradlemedium200.productcatalog.dto.CategoryDto;
import com.gradlemedium200.productcatalog.exception.CategoryNotFoundException;
import com.gradlemedium200.productcatalog.model.Category;
import com.gradlemedium200.productcatalog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for category management operations including hierarchical category handling.
 * Provides functionality for CRUD operations on categories and hierarchical category structure.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Create a new category.
     *
     * @param categoryDto The category data to create
     * @return The created category data
     */
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // Generate a unique ID for new category
        String categoryId = UUID.randomUUID().toString();
        
        // Create new category entity from DTO
        Category category = mapToEntity(categoryDto);
        category.setCategoryId(categoryId);
        
        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        
        // Calculate level in hierarchy
        if (category.getParentCategoryId() != null && !category.getParentCategoryId().isEmpty()) {
            // Find parent category to determine level
            Optional<Category> parentCategory = categoryRepository.findById(category.getParentCategoryId());
            if (parentCategory.isPresent()) {
                category.setLevel(parentCategory.get().getLevel() + 1);
            } else {
                // Parent not found, set as root category
                category.setParentCategoryId(null);
                category.setLevel(0);
            }
        } else {
            // Root category
            category.setLevel(0);
            category.setParentCategoryId(null);
        }
        
        // Set default active status if not specified
        category.setActive(true);
        
        // Save category to repository
        Category savedCategory = categoryRepository.save(category);
        
        // Return mapped DTO
        return mapToDto(savedCategory);
    }

    /**
     * Update an existing category.
     *
     * @param categoryId The ID of the category to update
     * @param categoryDto The updated category data
     * @return The updated category data
     * @throws CategoryNotFoundException if category not found
     */
    public CategoryDto updateCategory(String categoryId, CategoryDto categoryDto) {
        // Find existing category
        Category existingCategory = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        
        // Update category properties
        if (categoryDto.getName() != null) {
            existingCategory.setName(categoryDto.getName());
        }
        
        if (categoryDto.getDescription() != null) {
            existingCategory.setDescription(categoryDto.getDescription());
        }
        
        // Update parent category if changed
        if (categoryDto.getParentCategoryId() != null && 
            !categoryDto.getParentCategoryId().equals(existingCategory.getParentCategoryId())) {
            
            // Validate parent exists and isn't self or child category
            if (categoryDto.getParentCategoryId().equals(categoryId)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            
            // Check if new parent exists
            if (!categoryDto.getParentCategoryId().isEmpty()) {
                Optional<Category> newParent = categoryRepository.findById(categoryDto.getParentCategoryId());
                if (newParent.isPresent()) {
                    existingCategory.setParentCategoryId(categoryDto.getParentCategoryId());
                    existingCategory.setLevel(newParent.get().getLevel() + 1);
                    
                    // TODO: Update levels of all child categories recursively
                } else {
                    throw new CategoryNotFoundException(categoryDto.getParentCategoryId());
                }
            } else {
                // Setting as root category
                existingCategory.setParentCategoryId(null);
                existingCategory.setLevel(0);
                
                // TODO: Update levels of all child categories recursively
            }
        }
        
        // Update sort order
        existingCategory.setSortOrder(categoryDto.getSortOrder());
        
        // Set update timestamp
        existingCategory.setUpdatedAt(LocalDateTime.now());
        
        // Save updated category
        Category updatedCategory = categoryRepository.save(existingCategory);
        
        // Return mapped DTO
        return mapToDto(updatedCategory);
    }

    /**
     * Get a category by its ID.
     *
     * @param categoryId The ID of the category to retrieve
     * @return The category data
     * @throws CategoryNotFoundException if category not found
     */
    public CategoryDto getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        return mapToDto(category);
    }

    /**
     * Get all categories as a flat list.
     *
     * @return List of all categories
     */
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get the complete category hierarchy as a nested tree structure.
     * Root categories will contain their subcategories recursively.
     *
     * @return List of root categories with nested subcategories
     */
    public List<CategoryDto> getCategoryHierarchy() {
        // Get all categories first
        List<Category> allCategories = categoryRepository.findAll();
        
        // Find root categories (those with no parent)
        List<Category> rootCategories = allCategories.stream()
            .filter(category -> category.getParentCategoryId() == null || category.getParentCategoryId().isEmpty())
            .collect(Collectors.toList());
        
        // Create map of parent ID to list of child categories for efficient lookup
        Map<String, List<Category>> parentToChildrenMap = new HashMap<>();
        
        for (Category category : allCategories) {
            if (category.getParentCategoryId() != null && !category.getParentCategoryId().isEmpty()) {
                if (!parentToChildrenMap.containsKey(category.getParentCategoryId())) {
                    parentToChildrenMap.put(category.getParentCategoryId(), new ArrayList<>());
                }
                parentToChildrenMap.get(category.getParentCategoryId()).add(category);
            }
        }
        
        // Build hierarchy recursively starting with root categories
        List<CategoryDto> rootCategoryDtos = rootCategories.stream()
            .map(rootCategory -> buildCategoryHierarchy(rootCategory, parentToChildrenMap))
            .collect(Collectors.toList());
        
        // Sort by sortOrder
        rootCategoryDtos.sort(Comparator.comparingInt(
            categoryDto -> categoryDto.getSortOrder()));
            
        return rootCategoryDtos;
    }

    /**
     * Get direct subcategories of a parent category.
     *
     * @param parentCategoryId The parent category ID
     * @return List of child categories
     */
    public List<CategoryDto> getSubcategories(String parentCategoryId) {
        // Verify parent exists
        if (!categoryRepository.findById(parentCategoryId).isPresent()) {
            throw new CategoryNotFoundException(parentCategoryId);
        }
        
        // Find all subcategories
        List<Category> subcategories = categoryRepository.findByParentCategoryId(parentCategoryId);
        
        // Convert to DTOs
        List<CategoryDto> subcategoryDtos = subcategories.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
            
        // Sort by sort order
        subcategoryDtos.sort(Comparator.comparingInt(
            categoryDto -> categoryDto.getSortOrder()));
            
        return subcategoryDtos;
    }
    
    /**
     * Helper method to recursively build category hierarchy.
     *
     * @param category The current category to process
     * @param parentToChildrenMap Map of parent ID to children for lookup
     * @return DTO with complete hierarchy for the category
     */
    private CategoryDto buildCategoryHierarchy(Category category, Map<String, List<Category>> parentToChildrenMap) {
        CategoryDto dto = mapToDto(category);
        
        // Add subcategories if any
        if (parentToChildrenMap.containsKey(category.getCategoryId())) {
            List<Category> children = parentToChildrenMap.get(category.getCategoryId());
            
            children.sort(Comparator.comparingInt(Category::getSortOrder));
                
            // Process each child recursively
            List<CategoryDto> childDtos = children.stream()
                .map(child -> buildCategoryHierarchy(child, parentToChildrenMap))
                .collect(Collectors.toList());
                
            dto.setSubcategories(childDtos);
        } else {
            dto.setSubcategories(Collections.emptyList());
        }
        
        return dto;
    }
    
    /**
     * Map a Category entity to a CategoryDto.
     *
     * @param category The Category entity
     * @return The corresponding CategoryDto
     */
    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentCategoryId(category.getParentCategoryId());
        dto.setLevel(category.getLevel());
        dto.setSortOrder(category.getSortOrder());
        
        // Product count would typically be fetched from a repository
        // This is a placeholder for the actual implementation
        dto.setProductCount(0L); // FIXME: Implement actual product count lookup
        
        return dto;
    }
    
    /**
     * Map a CategoryDto to a Category entity.
     *
     * @param dto The CategoryDto
     * @return The corresponding Category entity
     */
    private Category mapToEntity(CategoryDto dto) {
        Category category = new Category();
        category.setCategoryId(dto.getCategoryId()); // Will be null for new categories
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setParentCategoryId(dto.getParentCategoryId());
        category.setLevel(dto.getLevel());
        category.setSortOrder(dto.getSortOrder());
        category.setActive(true); // Default to active
        
        return category;
    }
}