package com.ebudoskij.dessert_shop.service;

public interface LoginAttemptService {
    void checkAccountLocked(Long userId);

    short recordFailedAttempt(Long userId);

    void resetFailedAttempts(Long userId);
}
