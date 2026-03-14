package com.ebudoskij.dessert_shop.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
public class CookieUtils {
    @Value("${jwt.cookies.name}")
    private String JWT_NAME;

    @Value("${jwt.refresh.cookie.name}")
    private String JWT_REFRESH_NAME;

    @Value("${jwt.expiration}")
    private Duration JWT_COOKIE_MAX_AGE;

    @Value("${jwt.refresh.expiration}")
    private Duration JWT_REFRESH_COOKIE_MAX_AGE;

    public void createJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWT_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) JWT_COOKIE_MAX_AGE.toSeconds());
        // Helps prevent CSRF
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    public void createJwtRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWT_REFRESH_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) JWT_REFRESH_COOKIE_MAX_AGE.toSeconds());
        // Helps prevent CSRF
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    public String getJwtFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JWT_NAME);
        return (cookie != null) ? cookie.getValue() : null;
    }

    public String getJwtRefreshFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JWT_REFRESH_NAME);
        return (cookie != null) ? cookie.getValue() : null;
    }

    public void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWT_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // This deletes the cookie immediately
        response.addCookie(cookie);
    }

    public void clearJwtRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWT_REFRESH_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // This deletes the cookie immediately
        response.addCookie(cookie);
    }
}
