package com.ebudoskij.dessert_shop.model.dto.category;

import com.ebudoskij.dessert_shop.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDto {
    private Long id;

    private Long parentId;

    private String name;

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
