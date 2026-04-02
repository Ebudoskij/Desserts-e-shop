package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.OrderItem;
import com.ebudoskij.dessert_shop.model.OrderStatus;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.*;
import com.ebudoskij.dessert_shop.repository.OrderStatusRepository;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderStatusRepository orderStatusRepository;
    private final MediaService mediaService;

    // -----------------------------------------------------------------------
    // Order list
    // -----------------------------------------------------------------------

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public String fetchAll(@ModelAttribute OrderFilteringDto filter,
                           @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC)
                           Pageable pageable,
                           @AuthenticationPrincipal(expression = "id") Long userId,
                           Authentication auth,
                           @RequestParam(required = false) Boolean orderCreated,
                           Model model) {

        // Regular users only see their own orders
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            filter.setUserId(userId);
        }

        PageResponseDto<Order> response = orderService.getAll(filter, pageable);

        // All statuses for the filter dropdown
        List<OrderStatus> statuses = orderStatusRepository.findAll();

        model.addAttribute("pageResponse", response);
        model.addAttribute("orderStatuses", statuses);
        model.addAttribute("minPrice", orderService.getMinPrice());
        model.addAttribute("maxPrice", orderService.getMaxPrice());
        model.addAttribute("filter", filter);

        if (orderCreated != null && orderCreated) {
            model.addAttribute("orderCreated", true);
        }

        return "order/orders";
    }

    // -----------------------------------------------------------------------
    // Order detail
    // -----------------------------------------------------------------------

    @PreAuthorize("@orderSecurity.isOwnerOrAdmin(#id)")
    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id, Model model) {
        Order order = orderService.getById(id);

        // Load custom decor images for each item that has them
        Map<Long, List<MediaResponseDto>> customImages = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            if (Boolean.TRUE.equals(item.getCustomDecor()) && !Boolean.TRUE.equals(item.getIsDeleted())) {
                List<MediaResponseDto> images =
                        mediaService.getEntityImages("ORDER_ITEM_CUSTOM_IMAGE", item.getId());
                if (!images.isEmpty()) {
                    customImages.put(item.getId(), images);
                }
            }
        }

        // Build AdminReviewDTO pre-populated with item IDs so the form binds correctly
        AdminReviewDTO reviewDto = new AdminReviewDTO();
        List<AdminOrderItemReviewDto> reviewItems = order.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getCustomDecor()) && !Boolean.TRUE.equals(i.getIsDeleted()))
                .map(i -> {
                    AdminOrderItemReviewDto r = new AdminOrderItemReviewDto();
                    r.setOrderItemId(i.getId());
                    return r;
                })
                .toList();
        reviewDto.setItems(reviewItems);

        Map<Long, Integer> reviewIndexMap = new HashMap<>();
        for (int i = 0; i < reviewDto.getItems().size(); i++) {
            reviewIndexMap.put(reviewDto.getItems().get(i).getOrderItemId(), i);
        }
        model.addAttribute("response", order);
        model.addAttribute("customImages", customImages);
        model.addAttribute("reviewDto", reviewDto);
        model.addAttribute("reviewIndexMap", reviewIndexMap);
        return "order/order";
    }

    // -----------------------------------------------------------------------
    // Cart flow
    // -----------------------------------------------------------------------

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cart")
    public String viewCart(Model model) {
        Order cart = orderService.getCart();
        model.addAttribute("cart", cart);

        boolean hasCustomDecor = cart.getItems().stream()
                .anyMatch(i -> !i.getIsDeleted() && Boolean.TRUE.equals(i.getCustomDecor()));

        model.addAttribute("hasCustomDecor", hasCustomDecor);
        model.addAttribute("checkoutDto", new OrderCheckoutDto());
        return "order/cart";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cart/add")
    public String addToCart(@ModelAttribute("cartItem") @Valid CartItemCreateDto dto,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/products/" + dto.getProductId() + "?error=true";
        }
        orderService.addToCart(dto);
        return "redirect:/orders/cart";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cart/item/{itemId}/update")
    public String updateCartItem(@PathVariable Long itemId,
                                 @ModelAttribute @Valid CartItemUpdateDto dto,
                                 BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            orderService.updateCartItemQuantity(itemId, dto.getQuantity());
        }
        return "redirect:/orders/cart";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cart/item/{itemId}/delete")
    public String removeCartItem(@PathVariable Long itemId) {
        orderService.removeCartItem(itemId);
        return "redirect:/orders/cart";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cart/checkout")
    public String cartCheckout(@ModelAttribute("checkoutDto") @Valid OrderCheckoutDto dto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            Order cart = orderService.getCart();
            model.addAttribute("cart", cart);
            return "order/cart";
        }
        orderService.checkout(dto);
        return "redirect:/orders?orderCreated=true";
    }

    // -----------------------------------------------------------------------
    // Custom decor flow — admin review
    // -----------------------------------------------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/review")
    public String reviewOrder(@PathVariable Long id,
                              @ModelAttribute("reviewDto") @Valid AdminReviewDTO dto,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            // Re-render the order detail page with validation errors
            Order order = orderService.getById(id);
            Map<Long, List<MediaResponseDto>> customImages = new HashMap<>();
            for (OrderItem item : order.getItems()) {
                if (Boolean.TRUE.equals(item.getCustomDecor()) && !Boolean.TRUE.equals(item.getIsDeleted())) {
                    List<MediaResponseDto> images =
                            mediaService.getEntityImages("ORDER_ITEM_CUSTOM_IMAGE", item.getId());
                    if (!images.isEmpty()) {
                        customImages.put(item.getId(), images);
                    }
                }
            }
            model.addAttribute("response", order);
            model.addAttribute("customImages", customImages);
            model.addAttribute("reviewDto", dto);
            return "order/order";
        }
        orderService.reviewCustomOrder(id, dto);
        return "redirect:/orders/" + id + "?reviewed=true";
    }

    // -----------------------------------------------------------------------
    // Custom decor flow — user confirm / reject / cancel
    // -----------------------------------------------------------------------

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Long id) {
        orderService.confirmCustomOrder(id);
        return "redirect:/orders/" + id + "?confirmed=true";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/reject")
    public String rejectOrder(@PathVariable Long id) {
        orderService.rejectCustomOrder(id);
        return "redirect:/orders/" + id + "?rejected=true";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "redirect:/orders?orderCancelled=true";
    }

    // -----------------------------------------------------------------------
    // Admin delete
    // -----------------------------------------------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id) {
        orderService.deleteById(id);
        return "redirect:/orders";
    }
}
