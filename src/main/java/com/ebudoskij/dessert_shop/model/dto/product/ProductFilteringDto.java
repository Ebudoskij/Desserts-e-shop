package com.ebudoskij.dessert_shop.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductFilteringDto {
    private String searchQuery;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long categoryId;
    private Boolean deleted = false;
}
