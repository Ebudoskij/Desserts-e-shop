package com.ebudoskij.dessert_shop.config;

import com.ebudoskij.dessert_shop.model.Role;
import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.enums.RoleType;
import com.ebudoskij.dessert_shop.repository.RoleRepository;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfig adminConfig;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail(adminConfig.getEmail()).isEmpty()) {
            log.info("Creating initial admin user: {}", adminConfig.getEmail());

            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found in DB. Ensure Liquibase has run."));

            User admin = new User();
            admin.setEmail(adminConfig.getEmail());
            admin.setPasswordHash(passwordEncoder.encode(adminConfig.getPassword()));
            admin.setRoles(Set.of(adminRole));
            admin.setFullName("Admin");
            // Set other mandatory fields like 'enabled' or 'createdAt' if you have them

            userRepository.save(admin);
            log.info("Admin user created successfully.");
        }
    }
}
