package com.inditex.prices.application.port.out;

import com.inditex.prices.domain.model.Price;

import java.time.Instant;
import java.util.Optional;

public interface PriceRepository {
    Optional<Price> findApplicablePrice(Instant applicationDate, Long productId, Long brandId);
}
