package com.ebudoskij.dessert_shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String mainPage(){
        return "redirect:/products";
    }
}
