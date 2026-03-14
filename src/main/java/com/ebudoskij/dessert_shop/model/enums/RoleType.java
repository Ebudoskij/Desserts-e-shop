package com.ebudoskij.dessert_shop.model.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_USER(1),
    ROLE_ADMIN(2);

    private final int priority;

    RoleType(int priority) {
        this.priority = priority;
    }

}
