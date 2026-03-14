package com.ebudoskij.dessert_shop.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginException extends RuntimeException {
    private boolean locked;
    private short attempts;

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, boolean locked) {
        super(message);
        this.locked = locked;
    }

    public LoginException(String message, short attempts) {
        super(message);
        this.attempts = attempts;
    }
}
