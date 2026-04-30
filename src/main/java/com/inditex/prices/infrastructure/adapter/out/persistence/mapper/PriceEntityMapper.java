package com.inditex.prices.infrastructure.adapter.out.persistence.mapper;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface PriceEntityMapper {
    Price toDomain(PriceEntity entity);

    default OffsetDateTime map(LocalDateTime value) {
        return value == null
                ? null
                : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
