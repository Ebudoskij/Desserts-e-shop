package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.auth.LoginDto;
import com.ebudoskij.dessert_shop.model.dto.auth.RegisterDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {
    void loginUser(@Valid LoginDto loginDto,
                   HttpServletResponse response,
                   String ipAddress,
                   String userAgent);

    void registerUser(@Valid RegisterDto registerDto);

    void logout(HttpServletResponse response);
}
