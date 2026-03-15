package com.ebudoskij.dessert_shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.initial-admin")
@Getter
@Setter
public class AdminConfig {
    private String email;
    private String password;
}
