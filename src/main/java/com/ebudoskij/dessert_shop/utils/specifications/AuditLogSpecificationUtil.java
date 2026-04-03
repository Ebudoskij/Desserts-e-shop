package com.ebudoskij.dessert_shop.utils.specifications;

import com.ebudoskij.dessert_shop.model.AuditLog;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogFilteringDto;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AuditLogSpecificationUtil {

    public static Specification<AuditLog> buildFilters(AuditLogFilteringDto filter) {
        Specification<AuditLog> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.getSearchQuery())) {
            spec = spec.and(hasKeywordInEmailOrDetailsOrIp(filter.getSearchQuery()));
        }
        if (StringUtils.hasText(filter.getActionType())) {
            spec = spec.and(hasActionType(filter.getActionType()));
        }
        if (StringUtils.hasText(filter.getEntityType())) {
            spec = spec.and(hasEntityType(filter.getEntityType()));
        }
        if (filter.getEntityId() != null) {
            spec = spec.and(hasEntityId(filter.getEntityId()));
        }
        if (StringUtils.hasText(filter.getTimezone())) {
            if (filter.getMinDate() != null) {
                spec = spec.and(isAfterMinDate(toInstant(filter.getMinDate(), filter.getTimezone())));
            }
            if (filter.getMaxDate() != null) {
                spec = spec.and(isBeforeMaxDate(toInstant(filter.getMaxDate(), filter.getTimezone())));
            }
        }
        return spec;
    }

    /**
     * Full-text search across user email, details, and IP address.
     * Uses a LEFT JOIN on user so system-generated logs (user = null) are not excluded.
     */
    public static Specification<AuditLog> hasKeywordInEmailOrDetailsOrIp(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            // LEFT JOIN ensures rows where user IS NULL are returned for the detail/IP predicates
            var userJoin = root.join("user", JoinType.LEFT);
            if (query != null) query.distinct(true);
            return cb.or(
                cb.like(cb.lower(userJoin.<String>get("email")), pattern),
                cb.like(cb.lower(root.get("details")),   pattern),
                cb.like(cb.lower(root.get("ipAddress")), pattern)
            );
        };
    }

    /** Filter by exact {@link AuditActionType} name (case-sensitive enum match). */
    public static Specification<AuditLog> hasActionType(String actionType) {
        return (root, query, cb) -> {
            try {
                AuditActionType type = AuditActionType.valueOf(actionType);
                return cb.equal(root.get("actionType"), type);
            } catch (IllegalArgumentException e) {
                // Unknown enum value → return nothing
                return cb.disjunction();
            }
        };
    }

    /** Filter by entity type string (e.g. "Product", "Order"). */
    public static Specification<AuditLog> hasEntityType(String entityType) {
        return (root, query, cb) -> cb.equal(root.get("entityType"), entityType);
    }

    /** Filter by entity DB id. */
    public static Specification<AuditLog> hasEntityId(Long entityId) {
        return (root, query, cb) -> cb.equal(root.get("entityId"), entityId);
    }

    public static Specification<AuditLog> isAfterMinDate(Instant minInstant) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), minInstant);
    }

    public static Specification<AuditLog> isBeforeMaxDate(Instant maxInstant) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), maxInstant);
    }

    private static Instant toInstant(LocalDateTime localTime, String timezone) {
        return localTime.atZone(ZoneId.of(timezone)).toInstant();
    }
}
