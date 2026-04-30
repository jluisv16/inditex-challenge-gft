package com.inditex.prices.application.port.in;

import com.inditex.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface GetPriceQuery {
    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}