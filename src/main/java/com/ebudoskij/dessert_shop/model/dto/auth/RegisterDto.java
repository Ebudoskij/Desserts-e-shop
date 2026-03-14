package com.ebudoskij.dessert_shop.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
    @NotBlank(message = "Name cannot be blank!")
    private String fullName;

    @NotBlank
    @Email(message = "Enter a valid email, please!")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and a special character"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 12, message = "A valid phone number must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(\\+?38)?(?<operator>0\\d{2})[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$",
            message = "Please, enter a valid phone number"
    )
    private String phoneNumber;
}
