package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findAllByUserId(Long id);
    Optional<Order> findByUserIdAndStatusNameAndIsDeletedFalse(Long userId, String statusName);
}
