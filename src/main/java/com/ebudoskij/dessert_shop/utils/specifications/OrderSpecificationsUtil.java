package com.ebudoskij.dessert_shop.utils.specifications;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.order.OrderFilteringDto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class OrderSpecificationsUtil {

    public static Specification<Order> buildFilters(OrderFilteringDto filter) {
        Specification<Order> spec = Specification.unrestricted();

        if (filter.getDeleted() != null) {
            spec = spec.and(isDeleted(filter.getDeleted()));
        }

        if (filter.getMinPrice() != null) {
            spec = spec.and(hasPriceGreaterThanOrEqual(filter.getMinPrice()));
        }

        if (filter.getMaxPrice() != null) {
            spec = spec.and(hasPriceLessThanOrEqual(filter.getMaxPrice()));
        }

        if (filter.getUserId() != null) {
            spec = spec.and(hasUserId(filter.getUserId()));
        }

        if (filter.getOrderStatusId() != null) {
            spec = spec.and(hasStatusId(filter.getOrderStatusId()));
        }

        return spec;
    }

    public static Specification<Order> isDeleted(Boolean isDeleted) {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<Order> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice);
    }

    public static Specification<Order> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice);
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Order> hasStatusId(Long statusId) {
        return (root, query, cb) -> cb.equal(root.get("status").get("id"), statusId);
    }
}
