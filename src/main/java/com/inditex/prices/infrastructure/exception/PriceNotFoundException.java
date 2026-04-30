package com.inditex.prices.infrastructure.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long productId, Long brandId, LocalDateTime date) {
        super("No applicable price found for productId=%d, brandId=%d, date=%s"
                .formatted(productId, brandId, date));
    }
}
