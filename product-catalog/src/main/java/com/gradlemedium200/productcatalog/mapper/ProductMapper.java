package com.gradlemedium200.productcatalog.mapper;

import com.gradlemedium200.productcatalog.model.Product;
import com.gradlemedium200.productcatalog.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Product entities and ProductDto objects.
 * This class provides conversion methods to transform data between persistence
 * layer (Product entities) and presentation layer (ProductDto objects).
 */
public class ProductMapper {

    /**
     * Converts a Product entity to its corresponding ProductDto representation.
     * 
     * @param product the Product entity to convert
     * @return a new ProductDto populated with data from the Product entity
     * @throws IllegalArgumentException if product is null
     */
    public static ProductDto toDto(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        
        // Handle collections safely to avoid NPEs
        if (product.getImageUrls() != null) {
            dto.setImageUrls(new ArrayList<>(product.getImageUrls()));
        } else {
            dto.setImageUrls(new ArrayList<>());
        }
        
        if (product.getTags() != null) {
            dto.setTags(new HashSet<>(product.getTags()));
        } else {
            dto.setTags(new HashSet<>());
        }
        
        // Set additional status fields
        dto.setInStock("ACTIVE".equals(product.getStatus()));
        
        return dto;
    }

    /**
     * Converts a ProductDto to its corresponding Product entity representation.
     * Creates a new Product entity with the data from the provided DTO.
     * 
     * @param productDto the ProductDto to convert
     * @return a new Product entity populated with data from the ProductDto
     * @throws IllegalArgumentException if productDto is null
     */
    public static Product toEntity(ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("ProductDto cannot be null");
        }
        
        Product product = new Product();
        product.setProductId(productDto.getProductId());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategoryId(productDto.getCategoryId());
        product.setPrice(productDto.getPrice());
        product.setCurrency(productDto.getCurrency());
        
        // Set additional fields from DTO
        if (productDto.getImageUrls() != null) {
            product.setImageUrls(new ArrayList<>(productDto.getImageUrls()));
        }
        
        if (productDto.getTags() != null) {
            product.setTags(new HashSet<>(productDto.getTags()));
        }
        
        // Set status based on inStock flag
        product.setStatus(productDto.isInStock() ? "ACTIVE" : "INACTIVE");
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        return product;
    }

    /**
     * Converts a list of Product entities to a list of ProductDto objects.
     * 
     * @param products the list of Product entities to convert
     * @return a list of ProductDto objects
     */
    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return Collections.emptyList();
        }
        
        return products.stream()
                .filter(Objects::nonNull)
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing Product entity with data from a ProductDto.
     * This method is used when updating an existing product rather than creating a new one.
     * 
     * @param product the Product entity to update
     * @param productDto the ProductDto containing the new data
     * @throws IllegalArgumentException if either product or productDto is null
     */
    public static void updateEntityFromDto(Product product, ProductDto productDto) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productDto == null) {
            throw new IllegalArgumentException("ProductDto cannot be null");
        }
        
        // Update the fields that can be changed
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setCategoryId(productDto.getCategoryId());
        product.setPrice(productDto.getPrice());
        product.setCurrency(productDto.getCurrency());
        
        // Update collections
        if (productDto.getImageUrls() != null) {
            product.setImageUrls(new ArrayList<>(productDto.getImageUrls()));
        }
        
        if (productDto.getTags() != null) {
            product.setTags(new HashSet<>(productDto.getTags()));
        }
        
        // Update status based on inStock flag
        product.setStatus(productDto.isInStock() ? "ACTIVE" : "INACTIVE");
        
        // Update the timestamp to reflect the changes
        product.setUpdatedAt(LocalDateTime.now());
    }
    
    // TODO: Add support for mapping between Product entities and specialized DTOs
    // like ProductSummaryDto or ProductDetailsDto for different view requirements
    
    // TODO: Consider implementing bidirectional validation during mapping process
    
    // FIXME: The current implementation assumes CategoryName is set outside the mapper.
    // Need to refactor to include category name resolution logic or make it explicit
    // that CategoryName needs to be set separately.
}