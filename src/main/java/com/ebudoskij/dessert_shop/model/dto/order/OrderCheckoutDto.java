package com.ebudoskij.dessert_shop.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCheckoutDto {
    @NotBlank(message = "Delivery address is required")
    @Size(max = 200, message = "Delivery address must not exceed 200 characters")
    private String deliveryAddress;

    @NotBlank(message = "Delivery date is required")
    private String deliveryDate;
}
