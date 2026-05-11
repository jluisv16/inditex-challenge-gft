package com.inditex.prices.application.port.in;

import com.inditex.prices.domain.model.Price;

import java.time.Instant;

public interface GetPriceQuery {
    Price getApplicablePrice(Instant applicationDate, Long productId, Long brandId);
}