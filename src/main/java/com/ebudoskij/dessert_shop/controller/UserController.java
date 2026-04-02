package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.user.UserUpdateDto;
import com.ebudoskij.dessert_shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal(expression = "username") String email,
                            Model model) {
        var user = userService.getByEmail(email);
        model.addAttribute("response", user);
        return "user/user";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model) {
        model.addAttribute("response", userService.getById(id));

        return "user/user";
    }

    @GetMapping("/{id}/update")
    public String updateUserPage(@PathVariable Long id,
                                 Model model) {
        var response = userService.getById(id);
        model.addAttribute("response", response);
        
        UserUpdateDto userDto = new UserUpdateDto();
        userDto.setFullName(response.getFullName());
        userDto.setPhoneNumber(response.getPhoneNumber());
        model.addAttribute("user", userDto);

        return "user/updateUser";
    }

    @PatchMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") @Valid UserUpdateDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes ra) {

        if (bindingResult.hasErrors()){
            return "user/updateUser";
        }

        userService.updateById(id, dto);

        ra.addAttribute("id", id);

        return "redirect:/users/{id}";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);

        return "redirect: /auth/logout";
    }

}
