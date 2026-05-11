package com.inditex.prices.infrastructure.adapter.out.persistence.mapper;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Mapper(componentModel = "spring")
public interface PriceEntityMapper {
    Price toDomain(PriceEntity entity);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null
                ? null
                : instant.atOffset(ZoneOffset.UTC);
    }
}
