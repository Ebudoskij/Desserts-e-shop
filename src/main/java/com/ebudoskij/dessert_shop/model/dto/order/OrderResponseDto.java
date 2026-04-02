package com.ebudoskij.dessert_shop.model.dto.order;

import com.ebudoskij.dessert_shop.model.dto.user.UserResponseDto;
import com.ebudoskij.dessert_shop.model.dto.orderItem.OrderItemResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Long id;
    private UserResponseDto user;
    private String status;
    private String deliveryAddress;
    private Instant deliveryDate;
    private Instant createdAt;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalPrice;
}
