package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryCreateDto dto);
    
    @Mapping(target = "parentId", source = "parent.id")
    CategoryCreateDto toDto(Category entity);
    
    @Mapping(target = "parent", ignore = true)
    void updateEntityFromDto(CategoryCreateDto dto, @MappingTarget Category entity);
}
