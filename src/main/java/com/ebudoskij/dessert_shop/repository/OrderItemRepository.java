package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
