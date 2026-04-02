package com.ebudoskij.dessert_shop.model.dto.orderItem;

import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderItemResponseDto {
    private Long id;
    private ProductResponseDto product;
    private AdditionalItemResponseDto additionalItem;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    
    // Custom decor fields
    private Boolean customDecor;
    private String customDecorDescription;
    private BigDecimal customDecorPrice;
    private String adminComment;
    private List<MediaResponseDto> customImages;
}
