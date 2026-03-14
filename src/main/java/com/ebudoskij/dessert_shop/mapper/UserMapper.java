package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.dto.auth.RegisterDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(RegisterDto dto);
}
