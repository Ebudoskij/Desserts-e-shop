package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.*;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.AdditionalItemService;
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
@RequestMapping("/extras")
@RequiredArgsConstructor
public class AdditionalItemController {
    private final AdditionalItemService additionalItemService;
    private final CategoryService categoryService;

    @GetMapping
    public String fetchAll(@ModelAttribute AdditionalItemFilterDto filter,
                           @PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC)
                           Pageable pageable,
                           Model model){
        PageResponseDto<AdditionalItemCardDto> response = additionalItemService.getAll(filter, pageable);

        model.addAttribute("pageResponse", response);
        model.addAttribute("minPrice", additionalItemService.getMinPrice());
        model.addAttribute("maxPrice", additionalItemService.getMaxPrice());

        return "additionalItem/additionalItems";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model){
        AdditionalItemResponseDto response = additionalItemService.getById(id);

        model.addAttribute("response", response);

        return "additionalItem/additionalItem";
    }

    @GetMapping("/add")
    public String createAdditionalItemPage(Model model){
        model.addAttribute("additionalItem", new AdditionalItemCreateDto());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("unitTypes", UnitType.values());

        return "additionalItem/newAdditionalItem";
    }

    @PostMapping
    public String createAdditionalItem(@ModelAttribute("additionalItem") @Valid AdditionalItemCreateDto dto,
                                BindingResult bindingResult,
                                Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());

            return "additionalItem/newAdditionalItem";
        }

        additionalItemService.createAdditionalItem(dto);
        return "redirect:/extras";
    }

    @GetMapping("/{id}/update")
    public String updateAdditionalItemPage(@PathVariable Long id, Model model){
        model.addAttribute("additionalItemResponse", additionalItemService.getToUpdate(id));
        model.addAttribute("additionalItem", new AdditionalItemUpdateDto());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("unitTypes", UnitType.values());

        return "additionalItem/newAdditionalItem";
    }

    @PutMapping("/{id}")
    public String updateById(@PathVariable Long id,
                             @ModelAttribute("additionalItem") @Valid AdditionalItemUpdateDto dto,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());
            return "additionalItem/updateAdditionalItem";
        }

        additionalItemService.updateById(id, dto);

        return "redirect:/extras";
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id){
        additionalItemService.deleteById(id);
        return "redirect:/extras";
    }

    @PostMapping("/{id}/restore")
    public String restoreById(@PathVariable Long id){
        additionalItemService.restoreById(id);
        return "redirect:/extras";
    }
}
