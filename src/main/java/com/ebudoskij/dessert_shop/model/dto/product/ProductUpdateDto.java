package com.ebudoskij.dessert_shop.model.dto.product;

import com.ebudoskij.dessert_shop.model.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductUpdateDto {
    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal pricePerUnit;

    @NotNull
    private UnitType unitType;

    private List<MultipartFile> images;

    private List<Long> deletedImageIds;
}
