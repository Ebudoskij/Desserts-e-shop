package com.ebudoskij.dessert_shop.model.dto.category;

import com.ebudoskij.dessert_shop.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDto {
    private Long id;

    private Long parentId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public CategoryUpdateDto(Category category) {
        this.id = category.getId();

        if (category.getParent() != null){
            this.parentId = category.getParent().getId();
        }

        this.name = category.getName();
        this.description = category.getDescription();
    }
}
