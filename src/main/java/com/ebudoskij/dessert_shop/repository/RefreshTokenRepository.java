package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.RefreshTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {
    void deleteAllByUserId(Long id);

    Optional<RefreshTokens> findByTokenHash(String tokenHash);
}
