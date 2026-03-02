package com.ebudoskij.dessert_shop.model.dto.product;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import jakarta.persistence.*;

import java.math.BigDecimal;

public class ProductCreateDto {
    private Category category;

    private String name;

    private String description;

    private BigDecimal pricePerUnit;

    private UnitType unitType;
}
