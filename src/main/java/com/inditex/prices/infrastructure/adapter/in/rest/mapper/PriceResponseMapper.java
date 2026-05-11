package com.inditex.prices.infrastructure.adapter.in.rest.mapper;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface PriceResponseMapper {

    @Mapping(target = "startDate", expression = "java(toOffsetDateTime(price.startDate()))")
    @Mapping(target = "endDate", expression = "java(toOffsetDateTime(price.endDate()))")
    PriceResponse toResponse(Price price);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null
                ? null
                : instant.atOffset(ZoneOffset.UTC);
    }
}
