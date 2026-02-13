package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
}
