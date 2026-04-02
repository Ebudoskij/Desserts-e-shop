package com.ebudoskij.dessert_shop.model.dto.order;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderCreateDto {
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotBlank(message = "Delivery date is required")
    private String deliveryDate; // We will receive ISO string from datetime-local input

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
