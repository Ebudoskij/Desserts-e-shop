package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.OrderMapper;
import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.OrderCreateDto;
import com.ebudoskij.dessert_shop.repository.OrderRepository;
import com.ebudoskij.dessert_shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Override
    public void createOrder(OrderCreateDto dto) {
        Order order = orderMapper.toEntity(dto);
        order.setIsDeleted(false);
        // Note: Additional logic for parsing Status dropdown or inserting orderItems might be needed if they are set on create
        orderRepository.save(order);
    }

    @Override
    public void updateById(Long id, OrderCreateDto dto) {
        Order existingOrder = getById(id);
        orderMapper.updateEntityFromDto(dto, existingOrder);
        // Additional linking to OrderStatus or OrderItems if needed
        orderRepository.save(existingOrder);
    }

    @Override
    public void deleteById(Long id) {
        Order order = getById(id);
        order.setIsDeleted(true);
        orderRepository.save(order);
    }

    @Override
    public PageResponseDto<Order> getAll(int page, int size, String sortBy, String sortDir, String searchQuery) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            Specification<Order> notDeletedSpec = (r, q, cb) -> cb.isFalse(r.get("isDeleted"));

            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return notDeletedSpec.toPredicate(root, query, criteriaBuilder);
            }

            String pattern = "%" + searchQuery.toLowerCase() + "%";
            Specification<Order> searchSpec = (r, q, cb) -> cb.like(cb.lower(r.get("deliveryAddress")), pattern);

            return criteriaBuilder.and(
                    notDeletedSpec.toPredicate(root, query, criteriaBuilder),
                    searchSpec.toPredicate(root, query, criteriaBuilder)
            );
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageRequest);
        
        PageResponseDto<Order> response = new PageResponseDto<>();
        response.setContent(orderPage.getContent());
        response.setPageNo(orderPage.getNumber());
        response.setPageSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        response.setLast(orderPage.isLast());

        return response;
    }
}
