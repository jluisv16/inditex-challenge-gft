package com.inditex.prices.infrastructure.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Excepción lanzada cuando no se encuentra un precio aplicable.
 */
public class PriceNotFoundException extends RuntimeException {
    
    //private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private final Long productId;
    private final Long brandId;
    private final Instant applicationDate;

    public PriceNotFoundException(Long productId, Long brandId, Instant applicationDate) {
        super(buildMessage(productId, brandId, applicationDate, null));
        this.productId = productId;
        this.brandId = brandId;
        this.applicationDate = applicationDate;
    }
    
    public PriceNotFoundException(Long productId, Long brandId, Instant applicationDate, String customMessage) {
        super(customMessage != null ? customMessage : buildMessage(productId, brandId, applicationDate, null));
        this.productId = productId;
        this.brandId = brandId;
        this.applicationDate = applicationDate;
    }

    private static String buildMessage(Long productId, Long brandId, Instant applicationDate, String suffix) {
        String baseMessage = String.format(
            "No se encontró precio aplicable para productId=%d, brandId=%d, fecha=%s",
            productId,
            brandId,
            applicationDate
        );
        
        return suffix != null ? baseMessage + ". " + suffix : baseMessage;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public Instant getApplicationDate() {
        return applicationDate;
    }
}
