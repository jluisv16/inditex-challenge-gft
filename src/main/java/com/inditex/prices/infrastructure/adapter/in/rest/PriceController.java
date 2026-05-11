package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.application.usecase.GetPriceUseCase;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import com.inditex.prices.infrastructure.adapter.in.rest.generated.PricesApi;
import com.inditex.prices.infrastructure.adapter.in.rest.mapper.PriceResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Controlador REST para consulta de precios.
 * Implementa el contrato OpenAPI generado.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceController implements PricesApi {

    private final GetPriceUseCase getPriceUseCase;
    private final PriceResponseMapper mapper;

    @Override
    public ResponseEntity<PriceResponse> getPrice(OffsetDateTime applicationDate, Long productId, Long brandId) {
        log.info("GET /api/v1/prices - productId={}, brandId={}, applicationDate={}",
                productId, brandId, applicationDate);

        Instant instant = applicationDate.toInstant();

        var price = getPriceUseCase.getApplicablePrice(instant, productId, brandId);

        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
