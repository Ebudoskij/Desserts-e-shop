package com.ebudoskij.dessert_shop.model.dto.order;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CartItemCreateDto {
    @NotNull(message = "Product cannot be null")
    private Long productId;

    @NotNull(message = "Quantity must be specified")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Long additionalItemId;

    private Boolean customDecor = false;

    private String customDecorDescription;

    private List<MultipartFile> customImages;
}
