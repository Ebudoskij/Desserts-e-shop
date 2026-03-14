package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(@NotBlank @Email(message = "Введіть дійсну пошту!") String email);

    boolean existsByEmail(@NotBlank @Email(message = "Enter a valid email, please!") String email);
}
