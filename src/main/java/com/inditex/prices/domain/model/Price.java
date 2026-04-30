package com.inditex.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Value Object representing the applicable price for a product and brand at a given date.
 *
 * Immutable by design — no identity, no lifecycle, compared by value.
 * This is NOT a DDD Entity because it has no business identity and no state transitions.
 */
public record Price(
        Long productId,
        Long brandId,
        Integer priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency
) {}