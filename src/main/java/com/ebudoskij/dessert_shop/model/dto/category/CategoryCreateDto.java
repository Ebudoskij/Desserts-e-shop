package com.ebudoskij.dessert_shop.model.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDto {
    @NotBlank
    private String name;

    private Long parentId;

    private String description;
}
