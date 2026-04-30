package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.application.port.in.GetPriceQuery;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import com.inditex.prices.infrastructure.adapter.in.rest.generated.PricesApi;
import com.inditex.prices.infrastructure.adapter.in.rest.mapper.PriceResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class PriceController implements PricesApi {

    private final GetPriceQuery getPriceQuery;
    private final PriceResponseMapper mapper;

    @Override
    public ResponseEntity<PriceResponse> getPrice(OffsetDateTime applicationDate, Long productId, Long brandId) {
        return ResponseEntity.ok(
                mapper.toResponse(getPriceQuery.getApplicablePrice(applicationDate.toLocalDateTime(), productId, brandId))
        );
    }
}