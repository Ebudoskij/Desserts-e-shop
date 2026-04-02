package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.dto.auth.RegisterDto;
import com.ebudoskij.dessert_shop.model.dto.user.UserResponseDto;
import com.ebudoskij.dessert_shop.model.dto.user.UserUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(RegisterDto dto);

    UserResponseDto toDto(User user);

    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User entity);
}
