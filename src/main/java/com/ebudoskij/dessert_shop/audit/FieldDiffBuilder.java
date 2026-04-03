package com.ebudoskij.dessert_shop.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent builder that produces a Jackson {@link ObjectNode} describing only the
 * fields that changed between two states:
 * <pre>
 * {"name": {"from": "Old", "to": "New"}, "pricePerUnit": {"from": "10.00", "to": "15.00"}}
 * </pre>
 * Instantiate fresh per operation; not thread-safe.
 */
public class FieldDiffBuilder {

    private final Map<String, Object[]> changes = new LinkedHashMap<>();

    /**
     * Adds the field to the diff if oldVal and newVal are not equal.
     * Null values are included as the literal string "null".
     */
    public FieldDiffBuilder compare(String field, Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            changes.put(field, new Object[]{oldVal, newVal});
        }
        return this;
    }

    /** Returns {@code true} if at least one field differs. */
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    /**
     * Builds the diff as a Jackson {@link ObjectNode}.
     * Each entry: {@code "field": {"from": "...", "to": "..."}}.
     */
    public ObjectNode build(ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();
        changes.forEach((field, pair) -> {
            ObjectNode change = mapper.createObjectNode();
            change.put("from", pair[0] != null ? pair[0].toString() : null);
            change.put("to",   pair[1] != null ? pair[1].toString() : null);
            root.set(field, change);
        });
        return root;
    }

    /**
     * Convenience factory for an Order status transition diff:
     * <pre>{"status": {"from": "CONFIRMED", "to": "PAID"}}</pre>
     */
    public static ObjectNode statusChange(ObjectMapper mapper, String fromStatus, String toStatus) {
        return new FieldDiffBuilder()
                .compare("status", fromStatus, toStatus)
                .build(mapper);
    }
}
