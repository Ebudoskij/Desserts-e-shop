package com.ebudoskij.dessert_shop.model.enums;

public enum AuditActionType {
    /** Entity was first persisted (e.g. product created, order placed). */
    CREATED,
    /** One or more tracked fields were changed. */
    UPDATED,
    /** Entity was soft-deleted (isDeleted → true). */
    DELETED,
    /** Soft-delete was reversed (isDeleted → false). */
    RESTORED,
    /** Order moved to a new status. */
    STATUS_CHANGED
}
