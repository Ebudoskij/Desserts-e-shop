package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.OrderStatus;
import com.ebudoskij.dessert_shop.model.enums.OrderStatusType;
import com.ebudoskij.dessert_shop.repository.OrderRepository;
import com.ebudoskij.dessert_shop.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void rejectExpiredOrders() {
        OrderStatus waitingStatus = orderStatusRepository.findByName(OrderStatusType.WAITING_FOR_CLIENT_APPROVAL.name()).orElse(null);
        OrderStatus rejectedStatus = orderStatusRepository.findByName(OrderStatusType.REJECTED.name()).orElse(null);

        if (waitingStatus == null || rejectedStatus == null) return;

        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);

        // Filter orders that have been WAITING_FOR_CLIENT_APPROVAL for more than 24 hours
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> !o.getIsDeleted()
                        && o.getStatus().getId().equals(waitingStatus.getId())
                        && o.getUpdatedAt() != null
                        && o.getUpdatedAt().isBefore(threshold))
                .toList();

        for (Order o : orders) {
            o.setStatus(rejectedStatus);
            orderRepository.save(o);
        }
    }
}
