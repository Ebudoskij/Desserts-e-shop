package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.OrderCreateDto;

public interface OrderService {

    Order getById(Long id);

    void createOrder(OrderCreateDto dto);

    void updateById(Long id, OrderCreateDto dto);

    void deleteById(Long id);

    PageResponseDto<Order> getAll(int page, int size, String sortBy, String sortDir, String searchQuery);
}
