package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCreateDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductUpdateDto;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public String fetchAll(@RequestParam(required = false, defaultValue = "0") int page,
                                           @RequestParam(required = false, defaultValue = "10") int size,
                                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                           @RequestParam(required = false) String searchQuery,
                                           Model model){
        PageResponseDto<ProductResponseDto> response = productService.getAll(
                page,
                size,
                sortBy,
                sortDir,
                searchQuery);

        model.addAttribute("pageResponse", response);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchQuery", searchQuery);

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
}
