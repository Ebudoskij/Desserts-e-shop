package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.LoginException;
import com.ebudoskij.dessert_shop.exception.RegisterException;
import com.ebudoskij.dessert_shop.mapper.UserMapper;
import com.ebudoskij.dessert_shop.model.Role;
import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.dto.auth.LoginDto;
import com.ebudoskij.dessert_shop.model.dto.auth.RegisterDto;
import com.ebudoskij.dessert_shop.model.enums.RoleType;
import com.ebudoskij.dessert_shop.repository.RefreshTokenRepository;
import com.ebudoskij.dessert_shop.repository.RoleRepository;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.security.CookieUtils;
import com.ebudoskij.dessert_shop.security.JwtUtils;
import com.ebudoskij.dessert_shop.service.AuthService;
import com.ebudoskij.dessert_shop.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final UserMapper mapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public void loginUser(LoginDto loginDto,
                          HttpServletResponse response,
                          String ipAddress,
                          String userAgent) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new LoginException("Не існує користувача з такою поштою"));

        loginAttemptService.checkAccountLocked(user.getId());

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            short attempts = loginAttemptService.recordFailedAttempt(user.getId());
            throw new LoginException("Невірний пароль", attempts);
        }

        loginAttemptService.resetFailedAttempts(user.getId());

        RoleType highestRole = user.getRoles().stream()
                .map(Role::getName) // This gets the Enum from the Entity
                .max(Comparator.comparingInt(RoleType::getPriority))
                .orElse(RoleType.ROLE_USER);

        String token = jwtUtils.generateToken(loginDto.getEmail(), highestRole);

        cookieUtils.clearJwtRefreshCookie(response);
        refreshTokenRepository.deleteAllByUserId(user.getId());
        String refreshToken = jwtUtils.generateAndSaveRefreshToken(user,
                ipAddress,
                userAgent,
                highestRole);

        cookieUtils.createJwtCookie(response, token);
        cookieUtils.createJwtRefreshCookie(response, refreshToken);
    }

    @Override
    @Transactional
    public void registerUser(RegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RegisterException("Користувач з такою поштою вже існує");
        }

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());

        User newUser = mapper.toEntity(registerDto);
        newUser.setPasswordHash(encodedPassword);

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RegisterException("Помилка сервера: Роль за замовчуванням не знайдена."));
        newUser.setRoles(java.util.Set.of(userRole));

        userRepository.save(newUser);
    }

    @Override
    public void logout(HttpServletResponse response) {
        cookieUtils.clearJwtCookie(response);
        cookieUtils.clearJwtRefreshCookie(response);
        SecurityContextHolder.clearContext();
    }
}
