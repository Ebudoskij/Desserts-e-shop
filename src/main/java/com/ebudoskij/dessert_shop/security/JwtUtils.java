package com.ebudoskij.dessert_shop.security;

import com.ebudoskij.dessert_shop.exception.InvalidTokenException;
import com.ebudoskij.dessert_shop.model.RefreshTokens;
import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.model.enums.RoleType;
import com.ebudoskij.dessert_shop.repository.RefreshTokenRepository;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.utils.TokenHashUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final CookieUtils cookieUtils;

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expiration}")
    private Duration JWT_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    private Duration JWT_REFRESH_EXPIRATION;

    public String generateToken(String email, RoleType roleType) {
        return buildToken(email, roleType, JWT_EXPIRATION.toMillis());
    }

    public String generateRefreshToken(String email, RoleType roleType) {
        return buildToken(email, roleType, JWT_REFRESH_EXPIRATION.toMillis());
    }

    private String buildToken(String email, RoleType roleType, long JWT_EXPIRATIONTime) {
        return Jwts.builder()
                .subject(email)
                .claim("role", roleType.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATIONTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email)) && !isTokenExpired(token);
    }

    public String generateAndSaveRefreshToken(User user,
                                              String clientIp,
                                              String userAgent,
                                              RoleType roleType) {
        String refreshToken = generateRefreshToken(user.getEmail(), roleType);

        String tokenHash = TokenHashUtil.hashToken(refreshToken);

        RefreshTokens tokenEntity = RefreshTokens.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusSeconds(JWT_REFRESH_EXPIRATION.toSeconds()))
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(tokenEntity);

        return refreshToken;
    }

    @Transactional
    public String refreshToken(String refreshToken,
                               String ipAddress,
                               String userAgent,
                               HttpServletResponse response) {
        String tokenHash = TokenHashUtil.hashToken(refreshToken);

        RefreshTokens storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new InvalidTokenException("Invalid refresh token")
                );

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        String role = extractRole(refreshToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException(
                        "User with id " + storedToken.getUserId() + " not found"
                ));

        if (!isTokenValid(refreshToken, user.getEmail())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String newAccessToken = generateToken(user.getEmail(), RoleType.valueOf(role));
        String newRefreshToken = generateAndSaveRefreshToken(user,
                ipAddress,
                userAgent,
                RoleType.valueOf(role));

        cookieUtils.createJwtCookie(response, newAccessToken);
        cookieUtils.createJwtRefreshCookie(response, newRefreshToken);

        refreshTokenRepository.delete(storedToken);

        return newAccessToken;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
