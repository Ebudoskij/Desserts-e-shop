package com.ebudoskij.dessert_shop.exception;

import com.ebudoskij.dessert_shop.model.dto.auth.LoginDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    public ModelAndView handleLoginException(LoginException e, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("auth/login");
        mav.addObject("loginError", e.getMessage());

        if (e.isLocked()) {
            mav.addObject("isLocked", true);
        } else if (e.getAttempts() > 0) {
            mav.addObject("attempts", e.getAttempts());
            mav.addObject("remainingAttempts", 5 - e.getAttempts());
        }

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(request.getParameter("email"));
        mav.addObject("user", loginDto);

        return mav;
    }
}
