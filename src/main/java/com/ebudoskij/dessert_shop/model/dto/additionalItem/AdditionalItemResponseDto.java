package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AdditionalItemResponseDto {
    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal extraPrice;

    private List<String> imageUrls;
}
