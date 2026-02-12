package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
