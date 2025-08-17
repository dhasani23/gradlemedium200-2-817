package com.gradlemedium200.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for cart requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequestDto {
    
    private String userId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}