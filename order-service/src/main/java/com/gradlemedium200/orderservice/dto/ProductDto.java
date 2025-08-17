package com.gradlemedium200.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer inventoryCount;
    private String category;
    private boolean active;
}