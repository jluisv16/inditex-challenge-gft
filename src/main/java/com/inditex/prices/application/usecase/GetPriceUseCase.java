package com.inditex.prices.application.usecase;

import com.inditex.prices.application.port.in.GetPriceQuery;
import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.exception.PriceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GetPriceUseCase implements GetPriceQuery {

    private final PriceRepository priceRepository;

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return priceRepository.findApplicablePrice(applicationDate, productId, brandId)
                .orElseThrow(() -> new PriceNotFoundException(productId, brandId, applicationDate));
    }
}