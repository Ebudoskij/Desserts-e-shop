package com.ebudoskij.dessert_shop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 100)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}