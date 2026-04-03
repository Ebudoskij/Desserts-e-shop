package com.ebudoskij.dessert_shop.audit;

import com.ebudoskij.dessert_shop.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ThreadLocal holder for the current request's audit metadata.
 * Populated by {@link AuditContextAspect} before service method execution
 * and cleared in its finally block.
 */
public class AuditContextHolder {

    private static final ThreadLocal<AuditMetadata> CONTEXT = new ThreadLocal<>();

    public static void set(AuditMetadata metadata) {
        CONTEXT.set(metadata);
    }

    public static AuditMetadata get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Getter
    @AllArgsConstructor
    public static class AuditMetadata {
        /** Null for anonymous requests and scheduled system tasks. */
        private final User user;
        /** Null for scheduled system tasks (no HTTP request context). */
        private final String ipAddress;
    }
}
