package com.ebudoskij.dessert_shop.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String fullName;

    private String newPassword;

    private String confirmPassword;

    private String phoneNumber;
}
