package com.ebudoskij.dessert_shop.model.dto.product;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCardDto {
    @NotNull
    private Long id;

    @NotNull
    private Category category;

    @NotBlank
    private String name;

    @Positive
    private BigDecimal pricePerUnit;

    @NotNull
    private UnitType unitType;

    private Boolean isDeleted;

    private String mainImageUrl;
}
