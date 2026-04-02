package com.ebudoskij.dessert_shop.security;

import com.ebudoskij.dessert_shop.exception.InvalidTokenException;
import com.ebudoskij.dessert_shop.service.impl.CustomUserDetailsService;
import com.ebudoskij.dessert_shop.utils.HttpRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String jwt = cookieUtils.getJwtFromCookie(request);

        try {
            // 1. Try to authenticate with the Access Token
            if (jwt != null && jwtUtils.isTokenValid(jwt, jwtUtils.extractEmail(jwt))) {
                authenticateUser(jwt, request);
            }
            // 2. Access Token is missing or invalid; try the Refresh Token
            else {
                String refreshToken = cookieUtils.getJwtRefreshFromCookie(request);

                if (refreshToken != null) {
                    // This updates cookies in the 'response' object and DB
                    String newJwt = jwtUtils.refreshToken(
                            refreshToken,
                            HttpRequestUtils.getClientIp(request),
                            HttpRequestUtils.getUserAgent(request),
                            response
                    );

                    authenticateUser(newJwt, request);
                }
            }
        } catch (InvalidTokenException e) {
            SecurityContextHolder.clearContext();
            cookieUtils.clearJwtCookie(response);
            cookieUtils.clearJwtRefreshCookie(response);
        }
        catch (Exception e) {
            SecurityContextHolder.clearContext();

            logger.error(String.format("JWT Filter error on %s: %s - %s",
                    request.getRequestURI(),
                    e.getClass().getSimpleName(),
                    e.getMessage()));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String userEmail = jwtUtils.extractEmail(token);

        if (userEmail != null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,                    // ← was: userEmail (String)
                    null,
                    userDetails.getAuthorities()    // ← taken from loaded details, not the token
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
