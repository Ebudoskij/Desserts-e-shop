package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface OrderService {

    // --- Read ---
    Order getById(Long id);
    PageResponseDto<Order> getAll(OrderFilteringDto filter, Pageable pageable);
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();

    // --- Cart flow ---
    Order getCart();
    void addToCart(CartItemCreateDto dto);
    void updateCartItemQuantity(Long orderItemId, Integer newQuantity);
    void removeCartItem(Long orderItemId);
    void checkout(OrderCheckoutDto dto);

    // --- Custom decor flow ---
    void reviewCustomOrder(Long orderId, AdminReviewDTO dto);
    void confirmCustomOrder(Long orderId);
    void rejectCustomOrder(Long orderId);
    void cancelOrder(Long orderId);

    // --- Admin / general ---
    void updateById(Long id, OrderCheckoutDto dto);
    void deleteById(Long id);
}
