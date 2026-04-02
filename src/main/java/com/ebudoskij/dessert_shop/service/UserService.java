package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.user.UserResponseDto;
import com.ebudoskij.dessert_shop.model.dto.user.UserUpdateDto;
import jakarta.validation.Valid;

public interface UserService {
    UserResponseDto getById(Long id);

    UserResponseDto getByEmail(String email);

    void updateById(Long id, @Valid UserUpdateDto dto);

    void deleteById(Long id);
}
