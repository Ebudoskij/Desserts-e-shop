package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCreateDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductCreateDto dto);
    
    @Mapping(target = "images", ignore = true)
    ProductResponseDto toDto(Product entity);
    
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product entity);
}
