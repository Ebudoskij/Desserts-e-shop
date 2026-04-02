package com.ebudoskij.dessert_shop.security.modelSecurity;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    /**
     * Returns true if the currently authenticated user is the owner of the given order.
     * Intended for use with @PreAuthorize("@orderSecurity.isOwner(#orderId)")
     */
    public boolean isOwner(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        // The username stored in the security context is the user's email
        return order.getUser().getEmail().equals(auth.getName());
    }

    /**
     * Returns true if the currently authenticated user is the owner OR has the ADMIN role.
     * Intended for use with @PreAuthorize("@orderSecurity.isOwnerOrAdmin(#orderId)")
     */
    public boolean isOwnerOrAdmin(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        // Admins can always access any order
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        return order.getUser().getEmail().equals(auth.getName());
    }
}
