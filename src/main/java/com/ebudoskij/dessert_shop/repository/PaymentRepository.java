package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
