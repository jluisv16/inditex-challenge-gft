package com.inditex.prices.application.usecase;

import com.inditex.prices.application.port.in.GetPriceQuery;
import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.exception.PriceNotFoundException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPriceUseCase implements GetPriceQuery {

    private final PriceRepository priceRepository;

    @Override
    @Cacheable(
            value = "prices",
            key = "#applicationDate.toString() + '_' + #productId + '_' + #brandId"
    )
    @Retry(name = "priceService")
    @CircuitBreaker(name = "priceService", fallbackMethod = "getPriceFallback")
    @Bulkhead(name = "priceService")
    public Price getApplicablePrice(
            Instant applicationDate,
            Long productId,
            Long brandId
    ) {

        log.debug(
                "Searching applicable price for productId={}, brandId={}, date={}",
                productId,
                brandId,
                applicationDate
        );

        return priceRepository.findApplicablePrice(
                        applicationDate,
                        productId,
                        brandId
                )
                .orElseThrow(() ->
                        new PriceNotFoundException(
                                productId,
                                brandId,
                                applicationDate
                        )
                );
    }

    private Price getPriceFallback(
            Instant applicationDate,
            Long productId,
            Long brandId,
            Throwable ex
    ) {

        log.error(
                "Fallback triggered for productId={}, brandId={}, date={}, reason={}",
                productId,
                brandId,
                applicationDate,
                ex.getMessage()
        );

        throw new PriceNotFoundException(
                productId,
                brandId,
                applicationDate,
                "Service temporarily unavailable"
        );
    }
}