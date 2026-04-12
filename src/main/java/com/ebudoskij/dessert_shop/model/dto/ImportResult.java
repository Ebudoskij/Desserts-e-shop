package com.ebudoskij.dessert_shop.model.dto;

import java.util.List;

public record ImportResult(boolean success, int imported, List<String> errors) {

    public static ImportResult success(int count) {
        return new ImportResult(true, count, List.of());
    }

    public static ImportResult failure(List<String> errors) {
        return new ImportResult(false, 0, errors);
    }
}
