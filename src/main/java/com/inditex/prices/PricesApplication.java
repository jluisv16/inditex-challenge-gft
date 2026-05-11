package com.inditex.prices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PricesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricesApplication.class, args);
    }
}
