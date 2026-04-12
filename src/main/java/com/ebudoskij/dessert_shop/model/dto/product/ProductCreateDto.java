package com.ebudoskij.dessert_shop.model.dto.product;

import com.ebudoskij.dessert_shop.model.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductCreateDto {
    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price must be specified")
    @Positive(message = "Price per unit must be a positive number")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Unit type is required")
    private UnitType unitType;

    private Boolean customizable = true;

    private List<MultipartFile> images;

    private Integer mainImageIndex;
}
