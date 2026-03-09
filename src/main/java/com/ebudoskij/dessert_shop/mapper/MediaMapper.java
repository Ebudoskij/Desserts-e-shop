package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.Media;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaResponseDto toDto(Media media);
}
