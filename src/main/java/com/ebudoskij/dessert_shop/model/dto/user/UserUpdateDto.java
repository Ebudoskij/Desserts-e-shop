package com.ebudoskij.dessert_shop.model.dto.user;

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
public class UserUpdateDto {
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and a special character"
    )
    private String newPassword;

    private String confirmPassword;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
            regexp = "^(\\+?38)?(?<operator>0\\d{2})[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$",
            message = "Please, enter a valid phone number"
    )
    private String phoneNumber;
}
