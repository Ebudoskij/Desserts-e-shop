package com.ebudoskij.dessert_shop.model.dto.category;

public record CategoryStatDto(
        Long   id,
        Long   parentId,   // null if root
        String name,
        int    directCount
) {}
