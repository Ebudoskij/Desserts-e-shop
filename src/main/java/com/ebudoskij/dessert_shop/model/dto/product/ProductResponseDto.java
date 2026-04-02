package com.ebudoskij.dessert_shop.model.dto.product;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductResponseDto {
    @NotNull
    private Long id;

    @NotNull
    private Category category;

    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal pricePerUnit;

    @NotNull
    private UnitType unitType;

    private List<MediaResponseDto> images;

    private Long mainImageId;

    private Boolean isDeleted;

    private Boolean customizable;
}
