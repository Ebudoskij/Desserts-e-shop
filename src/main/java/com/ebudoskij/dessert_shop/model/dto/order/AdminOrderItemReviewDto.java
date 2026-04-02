package com.ebudoskij.dessert_shop.model.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminOrderItemReviewDto {

    @NotNull(message = "Order item ID must not be null")
    private Long orderItemId;

    @NotNull(message = "Custom decor price must not be null")
    @Positive(message = "Custom decor price must be positive")
    private BigDecimal customDecorPrice;

    private String adminComment;
}
