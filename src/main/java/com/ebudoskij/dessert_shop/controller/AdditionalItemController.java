package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemCreateDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemUpdateDto;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.AdditionalItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/extras")
public class AdditionalItemController {
    AdditionalItemService additionalItemService;
    CategoryService categoryService;

    @GetMapping
    public String fetchAll(@RequestParam(required = false, defaultValue = "0") int page,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           @RequestParam(required = false) String sortBy,
                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                           @RequestParam(required = false) String searchQuery,
                           Model model){
        PageResponseDto<AdditionalItem> response = additionalItemService.getAll(
                page,
                size,
                sortBy,
                sortDir,
                searchQuery);

        model.addAttribute("pageResponse", response);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchQuery", searchQuery);

        return "additionalItem/additionalItems";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model){
        AdditionalItem response = additionalItemService.getById(id);

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
        return "redirect:/additionalItems";
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

        return "redirect:/additionalItems";
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id){
        additionalItemService.deleteById(id);
        return "redirect:/additionalItems";
    }
}
