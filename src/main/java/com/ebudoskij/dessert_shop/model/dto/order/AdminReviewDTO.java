package com.ebudoskij.dessert_shop.model.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminReviewDTO {

    @NotEmpty(message = "At least one item review is required")
    @Valid
    private List<AdminOrderItemReviewDto> items;
}
