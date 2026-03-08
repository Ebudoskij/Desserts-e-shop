package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemCreateDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdditionalItemMapper {
    AdditionalItem toEntity(AdditionalItemCreateDto dto);
    
    @org.mapstruct.Mapping(target = "imageUrls", ignore = true)
    AdditionalItemResponseDto toDto(AdditionalItem entity);
    
    void updateEntityFromDto(AdditionalItemUpdateDto dto, @MappingTarget AdditionalItem entity);
}
