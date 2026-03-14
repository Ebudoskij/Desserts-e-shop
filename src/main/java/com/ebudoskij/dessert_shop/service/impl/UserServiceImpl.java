package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
}
