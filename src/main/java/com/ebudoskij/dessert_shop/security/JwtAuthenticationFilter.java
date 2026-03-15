package com.ebudoskij.dessert_shop.security;

import com.ebudoskij.dessert_shop.exception.InvalidTokenException;
import com.ebudoskij.dessert_shop.utils.HttpRequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;

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
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String userEmail = jwtUtils.extractEmail(token);
        String roleString = jwtUtils.extractRole(token);

        if (userEmail != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleString);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    List.of(authority)
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
