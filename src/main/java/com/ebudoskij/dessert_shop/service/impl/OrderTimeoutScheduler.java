package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.audit.AuditLogHelper;
import com.ebudoskij.dessert_shop.audit.FieldDiffBuilder;
import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.OrderStatus;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.model.enums.OrderStatusType;
import com.ebudoskij.dessert_shop.repository.OrderRepository;
import com.ebudoskij.dessert_shop.repository.OrderStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final AuditLogHelper auditLogHelper;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void rejectExpiredOrders() {
        OrderStatus waitingStatus = orderStatusRepository
                .findByName(OrderStatusType.WAITING_FOR_CLIENT_APPROVAL.name()).orElse(null);
        OrderStatus rejectedStatus = orderStatusRepository
                .findByName(OrderStatusType.REJECTED.name()).orElse(null);

        if (waitingStatus == null || rejectedStatus == null) return;

        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> !o.getIsDeleted()
                        && o.getStatus().getId().equals(waitingStatus.getId())
                        && o.getUpdatedAt() != null
                        && o.getUpdatedAt().isBefore(threshold))
                .toList();

        for (Order o : orders) {
            String oldStatus = o.getStatus().getName();
            o.setStatus(rejectedStatus);
            orderRepository.save(o);

            // user=null and ipAddress=null because this runs as a system scheduled task.
            // AuditContextHolder will have null metadata (no HTTP thread, no security context).
            auditLogHelper.log(
                    "Order",
                    o.getId(),
                    AuditActionType.STATUS_CHANGED,
                    FieldDiffBuilder.statusChange(objectMapper, oldStatus, OrderStatusType.REJECTED.name()),
                    "Order #" + o.getId() + " was auto-rejected after 24h client approval timeout"
            );
        }
    }
}
