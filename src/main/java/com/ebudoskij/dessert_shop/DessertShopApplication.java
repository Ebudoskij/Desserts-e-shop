package com.ebudoskij.dessert_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DessertShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(DessertShopApplication.class, args);
	}

}
