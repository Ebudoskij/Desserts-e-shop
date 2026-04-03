package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.audit.AuditLogHelper;
import com.ebudoskij.dessert_shop.audit.FieldDiffBuilder;
import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.model.*;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.*;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.model.enums.OrderStatusType;
import com.ebudoskij.dessert_shop.repository.*;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.service.OrderService;
import com.ebudoskij.dessert_shop.utils.specifications.OrderSpecificationsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ENTITY_TYPE = "Order";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AdditionalItemRepository additionalItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final AuditLogHelper auditLogHelper;
    private final ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Override
    public PageResponseDto<Order> getAll(OrderFilteringDto filter, Pageable pageable) {
        Specification<Order> spec = OrderSpecificationsUtil.buildFilters(filter);
        Page<Order> page = orderRepository.findAll(spec, pageable);
        return new PageResponseDto<>(page);
    }

    @Override
    public BigDecimal getMinPrice() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMaxPrice() {
        return BigDecimal.ZERO;
    }

    // -------------------------------------------------------------------------
    // Cart flow
    // -------------------------------------------------------------------------

    @Override
    public Order getCart() {
        User user = getCurrentUser();
        return orderRepository
                .findByUserIdAndStatusNameAndIsDeletedFalse(user.getId(), OrderStatusType.CREATED.name())
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setUser(user);
                    newOrder.setStatus(getStatus(OrderStatusType.CREATED));
                    newOrder.setIsDeleted(false);
                    newOrder.setTotalPrice(BigDecimal.ZERO);
                    return orderRepository.save(newOrder);
                    // Cart creation is NOT audited here — it is an internal implementation detail.
                    // The business-meaningful event is recorded at checkout().
                });
    }

    @Override
    @Transactional
    public void addToCart(CartItemCreateDto dto) {
        Order cart = getCart();

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!product.getCustomizable() && Boolean.TRUE.equals(dto.getCustomDecor())) {
            throw new IllegalArgumentException("This product is not customizable.");
        }

        AdditionalItem additionalItem = null;
        if (dto.getAdditionalItemId() != null) {
            additionalItem = additionalItemRepository.findById(dto.getAdditionalItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Additional item not found"));

            if (Boolean.TRUE.equals(dto.getCustomDecor())) {
                throw new IllegalArgumentException("Cannot select both standard additional item and custom decor.");
            }
        }

        OrderItem item = new OrderItem();
        item.setOrder(cart);
        item.setProduct(product);
        item.setAdditionalItem(additionalItem);
        item.setQuantity(dto.getQuantity());
        item.setIsDeleted(false);
        item.setCustomDecor(dto.getCustomDecor());
        item.setCustomDecorDescription(dto.getCustomDecorDescription());

        BigDecimal productPriceTotal = product.getPricePerUnit().multiply(BigDecimal.valueOf(dto.getQuantity()));
        if (additionalItem != null && !dto.getCustomDecor()) {
            BigDecimal extrasTotal = additionalItem.getExtraPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            item.setPriceAtPurchase(productPriceTotal.add(extrasTotal));
        } else {
            item.setPriceAtPurchase(productPriceTotal);
        }

        item = orderItemRepository.save(item);

        if (Boolean.TRUE.equals(dto.getCustomDecor()) && dto.getCustomImages() != null && !dto.getCustomImages().isEmpty()) {
            mediaService.saveEntityImages("ORDER_ITEM_CUSTOM_IMAGE", item.getId(), dto.getCustomImages(), 0);
        }

        if (cart.getItems() != null && !cart.getItems().contains(item)) {
            cart.getItems().add(item);
        } else if (cart.getItems() == null) {
            cart.setItems(new java.util.ArrayList<>(List.of(item)));
        }
        recalculateOrderTotal(cart);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long orderItemId, Integer newQuantity) {
        Order cart = getCart();
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        if (!item.getOrder().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to your cart");
        }

        item.setQuantity(newQuantity);

        BigDecimal productPriceTotal = item.getProduct().getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity));
        if (item.getAdditionalItem() != null && !item.getCustomDecor()) {
            BigDecimal extrasTotal = item.getAdditionalItem().getExtraPrice().multiply(BigDecimal.valueOf(newQuantity));
            item.setPriceAtPurchase(productPriceTotal.add(extrasTotal));
        } else {
            item.setPriceAtPurchase(productPriceTotal);
        }

        orderItemRepository.save(item);
        cart = orderRepository.findById(cart.getId()).orElseThrow();
        recalculateOrderTotal(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long orderItemId) {
        Order cart = getCart();
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        if (!item.getOrder().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to your cart");
        }

        item.setIsDeleted(true);
        orderItemRepository.save(item);

        cart = orderRepository.findById(cart.getId()).orElseThrow();
        recalculateOrderTotal(cart);
    }

    @Override
    @Transactional
    public void checkout(OrderCheckoutDto dto) {
        Order cart = getCart();

        if (cart.getItems() == null || cart.getItems().stream().allMatch(OrderItem::getIsDeleted)) {
            throw new IllegalStateException("Cart is empty");
        }

        cart.setDeliveryAddress(dto.getDeliveryAddress());
        cart.setDeliveryDate(LocalDateTime.parse(dto.getDeliveryDate()).atZone(ZoneId.systemDefault()).toInstant());

        boolean hasCustomDecor = cart.getItems().stream()
                .filter(i -> !i.getIsDeleted())
                .anyMatch(item -> Boolean.TRUE.equals(item.getCustomDecor()));

        OrderStatusType newStatusType = hasCustomDecor
                ? OrderStatusType.PENDING_CUSTOM_REVIEW
                : OrderStatusType.CONFIRMED;

        cart.setStatus(getStatus(newStatusType));
        orderRepository.save(cart);

        // ── Audit: order placed (CREATED from a business perspective) ──
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("status",          newStatusType.name());
        snapshot.put("deliveryAddress", cart.getDeliveryAddress());
        snapshot.put("totalPrice",      cart.getTotalPrice() != null ? cart.getTotalPrice().toPlainString() : "0");

        auditLogHelper.log(ENTITY_TYPE, cart.getId(), AuditActionType.CREATED,
                snapshot,
                "Order #" + cart.getId() + " was placed with status " + newStatusType.name());
    }

    // -------------------------------------------------------------------------
    // Custom decor flow
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void reviewCustomOrder(Long orderId, AdminReviewDTO dto) {
        Order order = getById(orderId);
        if (!order.getStatus().getName().equals(OrderStatusType.PENDING_CUSTOM_REVIEW.name())) {
            throw new IllegalStateException("Order is not pending custom review");
        }
        String oldStatus = order.getStatus().getName();

        Map<Long, OrderItem> itemMap = order.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getCustomDecor()) && !Boolean.TRUE.equals(i.getIsDeleted()))
                .collect(Collectors.toMap(OrderItem::getId, i -> i));

        for (AdminOrderItemReviewDto reviewItem : dto.getItems()) {
            OrderItem item = itemMap.get(reviewItem.getOrderItemId());
            if (item == null) {
                throw new EntityNotFoundException("Custom decor order item not found: " + reviewItem.getOrderItemId());
            }
            item.setCustomDecorPrice(reviewItem.getCustomDecorPrice());
            item.setAdminComment(reviewItem.getAdminComment());

            BigDecimal decorExtra = reviewItem.getCustomDecorPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal baseProductPrice = item.getProduct().getPricePerUnit()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setPriceAtPurchase(baseProductPrice.add(decorExtra));

            orderItemRepository.save(item);
        }

        order = orderRepository.findById(orderId).orElseThrow();
        recalculateOrderTotal(order);

        order.setStatus(getStatus(OrderStatusType.WAITING_FOR_CLIENT_APPROVAL));
        orderRepository.save(order);

        auditLogHelper.log(ENTITY_TYPE, orderId, AuditActionType.STATUS_CHANGED,
                FieldDiffBuilder.statusChange(objectMapper, oldStatus, OrderStatusType.WAITING_FOR_CLIENT_APPROVAL.name()),
                "Order #" + orderId + " reviewed by admin, awaiting client approval");
    }

    @Override
    @Transactional
    @PreAuthorize("@orderSecurity.isOwner(#orderId)")
    public void confirmCustomOrder(Long orderId) {
        Order order = getById(orderId);
        if (!order.getStatus().getName().equals(OrderStatusType.WAITING_FOR_CLIENT_APPROVAL.name())) {
            throw new IllegalStateException("Order is not awaiting client approval");
        }
        String oldStatus = order.getStatus().getName();
        order.setStatus(getStatus(OrderStatusType.CONFIRMED));
        orderRepository.save(order);

        auditLogHelper.log(ENTITY_TYPE, orderId, AuditActionType.STATUS_CHANGED,
                FieldDiffBuilder.statusChange(objectMapper, oldStatus, OrderStatusType.CONFIRMED.name()),
                "Order #" + orderId + " confirmed by client");
    }

    @Override
    @Transactional
    @PreAuthorize("@orderSecurity.isOwner(#orderId)")
    public void rejectCustomOrder(Long orderId) {
        Order order = getById(orderId);
        if (!order.getStatus().getName().equals(OrderStatusType.WAITING_FOR_CLIENT_APPROVAL.name())) {
            throw new IllegalStateException("Order is not awaiting client approval");
        }
        String oldStatus = order.getStatus().getName();
        order.setStatus(getStatus(OrderStatusType.REJECTED));
        orderRepository.save(order);

        auditLogHelper.log(ENTITY_TYPE, orderId, AuditActionType.STATUS_CHANGED,
                FieldDiffBuilder.statusChange(objectMapper, oldStatus, OrderStatusType.REJECTED.name()),
                "Order #" + orderId + " rejected by client");
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getById(orderId);
        String oldStatus = order.getStatus().getName();
        order.setStatus(getStatus(OrderStatusType.CANCELLED));
        orderRepository.save(order);

        auditLogHelper.log(ENTITY_TYPE, orderId, AuditActionType.STATUS_CHANGED,
                FieldDiffBuilder.statusChange(objectMapper, oldStatus, OrderStatusType.CANCELLED.name()),
                "Order #" + orderId + " was cancelled");
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    @Override
    public void updateById(Long id, OrderCheckoutDto dto) {
        throw new UnsupportedOperationException("Standard update not available via generic form.");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Order order = getById(id);
        order.setIsDeleted(true);
        orderRepository.save(order);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.DELETED,
                new FieldDiffBuilder().compare("isDeleted", false, true).build(objectMapper),
                "Order #" + id + " was soft-deleted");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private OrderStatus getStatus(OrderStatusType type) {
        return orderStatusRepository.findByName(type.name())
                .orElseThrow(() -> new EntityNotFoundException("Order status not found: " + type.name()));
    }

    private void recalculateOrderTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (!item.getIsDeleted() && item.getPriceAtPurchase() != null) {
                    total = total.add(item.getPriceAtPurchase());
                }
            }
        }
        order.setTotalPrice(total);
        orderRepository.save(order);
    }
}
