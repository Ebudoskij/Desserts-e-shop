package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.auth.LoginDto;
import com.ebudoskij.dessert_shop.model.dto.auth.RegisterDto;
import com.ebudoskij.dessert_shop.service.AuthService;
import com.ebudoskij.dessert_shop.utils.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage (@RequestParam(value = "logout", required = false) Boolean isLoggedOut,
                             Model model) {

        if (isLoggedOut != null && isLoggedOut){
            model.addAttribute("logout", true);
        }

        model.addAttribute("user", new LoginDto());

        return "auth/login";
    }

    @PostMapping("/login")
    public String login (@ModelAttribute("user") @Valid LoginDto loginDto,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         HttpServletResponse response) {

        if (bindingResult.hasErrors()){
            return "auth/login";
        }

        authService.loginUser(loginDto,
                response,
                HttpRequestUtils.getClientIp(request),
                HttpRequestUtils.getUserAgent(request));

        return  "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage (Model model) {
        model.addAttribute("user", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/clients/register")
    public String register(@ModelAttribute("user") @Valid RegisterDto registerDto,
                           BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "auth/register";
        }

        authService.registerUser(registerDto);

        return "redirect:/auth/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response,
                         RedirectAttributes ra) {

        authService.logout(response);

        ra.addAttribute("logout", "true");

        return "redirect:/auth/login";
    }
}
