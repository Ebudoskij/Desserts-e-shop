package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdditionalItemFilterDto {
    private String searchQuery;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean deleted = false;
}
