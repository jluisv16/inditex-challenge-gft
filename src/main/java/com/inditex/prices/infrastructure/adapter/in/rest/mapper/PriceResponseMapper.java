package com.inditex.prices.infrastructure.adapter.in.rest.mapper;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface PriceResponseMapper {
    PriceResponse toResponse(Price price);

    default OffsetDateTime map(LocalDateTime value) {
        return value == null
                ? null
                : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
