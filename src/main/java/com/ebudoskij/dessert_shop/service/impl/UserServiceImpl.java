package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.UserMapper;
import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.dto.user.UserResponseDto;
import com.ebudoskij.dessert_shop.model.dto.user.UserUpdateDto;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));

        return userMapper.toDto(user);
    }

    @Override
    public void updateById(Long id, UserUpdateDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with " + id + " not found"));

        userMapper.updateEntityFromDto(dto, existingUser);

        if (dto.getNewPassword() != null) {
            existingUser.setPasswordHash(
                    passwordEncoder.encode(
                            dto.getNewPassword()
                    )
            );
        }

        userRepository.save(existingUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
