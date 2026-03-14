package com.ebudoskij.dessert_shop.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @NotBlank
    @Email(message = "Enter a valid email, please!")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
