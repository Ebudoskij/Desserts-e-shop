package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.LoginException;
import com.ebudoskij.dessert_shop.model.LoginInfo;
import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.repository.LoginInfoRepository;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final short MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_DAYS = 1;

    private final LoginInfoRepository loginInfoRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAccountLocked(Long userId) {
        LoginInfo loginInfo = loginInfoRepository.findById(userId)
                .orElse(null);

        if (loginInfo == null || !loginInfo.getLocked()) {
            return;
        }

        Instant lockExpiration = loginInfo.getLockDate().plus(LOCK_DURATION_DAYS, ChronoUnit.DAYS);

        if (Instant.now().isAfter(lockExpiration)) {
            loginInfo.setLocked(false);
            loginInfo.setFailedAttempts((short) 0);
            loginInfo.setLockDate(null);
            loginInfoRepository.save(loginInfo);
            return;
        }

        throw new LoginException("Account is locked. Try again later.", true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public short recordFailedAttempt(Long userId) {
        LoginInfo loginInfo = loginInfoRepository.findById(userId)
                .orElseGet(() -> {
                    // getReferenceById creates a proxy, avoiding an extra SELECT query
                    User userProxy = userRepository.getReferenceById(userId);
                    return new LoginInfo(userProxy);
                });

        short newAttempts = (short) (loginInfo.getFailedAttempts() + 1);
        loginInfo.setFailedAttempts(newAttempts);

        if (newAttempts >= MAX_ATTEMPTS) {
            loginInfo.setLocked(true);
            loginInfo.setLockDate(Instant.now());
        }

        loginInfoRepository.save(loginInfo);
        return newAttempts;
    }

    @Override
    public void resetFailedAttempts(Long userId) {
        loginInfoRepository.findById(userId).ifPresent(loginInfo -> {
            if (loginInfo.getFailedAttempts() > 0) {
                loginInfo.setFailedAttempts((short) 0);
                loginInfo.setLocked(false);
                loginInfo.setLockDate(null);
                loginInfoRepository.save(loginInfo);
            }
        });
    }
}
