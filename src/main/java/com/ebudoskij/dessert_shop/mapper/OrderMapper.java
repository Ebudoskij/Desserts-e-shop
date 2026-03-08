package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.order.OrderCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity(OrderCreateDto dto);
    
    void updateEntityFromDto(OrderCreateDto dto, @MappingTarget Order entity);
}
