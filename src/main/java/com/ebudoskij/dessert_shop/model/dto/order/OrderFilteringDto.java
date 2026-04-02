package com.ebudoskij.dessert_shop.model.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilteringDto {
    private String searchQuery;
    private Long orderStatusId;
    private Long userId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean deleted;
}
