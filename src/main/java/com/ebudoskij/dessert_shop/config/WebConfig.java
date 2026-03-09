package com.ebudoskij.dessert_shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps the URL path /uploads/** to the physical folder in your project root
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
