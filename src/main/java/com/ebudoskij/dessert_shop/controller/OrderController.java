package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.Order;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.order.OrderCreateDto;
import com.ebudoskij.dessert_shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public String fetchAll(@RequestParam(required = false, defaultValue = "0") int page,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                           @RequestParam(required = false) String searchQuery,
                           Model model){
        PageResponseDto<Order> response = orderService.getAll(
                page,
                size,
                sortBy,
                sortDir,
                searchQuery);

        model.addAttribute("pageResponse", response);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchQuery", searchQuery);

        return "order/orders";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model){
        Order response = orderService.getById(id);

        model.addAttribute("response", response);

        return "order/order";
    }

    @GetMapping("/add")
    public String createOrderPage(Model model){
        model.addAttribute("order", new OrderCreateDto());

        return "order/newOrder";
    }

    @PostMapping
    public String createOrder(@ModelAttribute("order") @Valid OrderCreateDto dto,
                                 BindingResult bindingResult,
                                 Model model){
        if (bindingResult.hasErrors()) {

            return "order/newOrder";
        }

        orderService.createOrder(dto);
        return "redirect:/orders";
    }



    @PutMapping("/{id}")
    public String updateById(@PathVariable Long id,
                             @ModelAttribute("order") @Valid OrderCreateDto dto,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            return "order/updateOrder";
        }

        orderService.updateById(id, dto);

        return "redirect:/orders";
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id){
        orderService.deleteById(id);
        return "redirect:/orders";
    }
}
