package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.*;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String fetchAll(@ModelAttribute ProductFilteringDto filter,
                           @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC)
                           Pageable pageable,
                           Model model){
        PageResponseDto<ProductCardDto> response = productService.getAll(filter, pageable);

        model.addAttribute("pageResponse", response);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("minPrice", productService.getMinPrice());
        model.addAttribute("maxPrice", productService.getMaxPrice());

        return "product/products";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model){
        ProductResponseDto response = productService.getById(id);

        model.addAttribute("response", response);

        return "product/product";
    }

    @GetMapping("/add")
    public String createProductPage(Model model){
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("unitTypes", UnitType.values());

        return "product/newProduct";
    }

    @PostMapping
    public String createProduct(@ModelAttribute("product") @Valid ProductCreateDto dto,
                                BindingResult bindingResult,
                                Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());

            return "product/newProduct";
        }

        productService.createProduct(dto);
        return "redirect:/products";
    }

    @GetMapping("/{id}/update")
    public String updateProductPage(@PathVariable Long id, Model model){
        model.addAttribute("productResponse", productService.getToUpdate(id));
        model.addAttribute("product", new ProductUpdateDto());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("unitTypes", UnitType.values());

        return "product/newProduct";
    }

    @PutMapping("/{id}")
    public String updateById(@PathVariable Long id,
                             @ModelAttribute("product") @Valid ProductUpdateDto dto,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());
            return "product/updateProduct";
        }

        productService.updateById(id, dto);

        return "redirect:/products";
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id){
        productService.deleteById(id);
        return "redirect:/products";
    }

    @PostMapping("/{id}/restore")
    public String restoreById(@PathVariable Long id){
        productService.restoreById(id);
        return "redirect:/products";
    }
}
