package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper;
import com.inditex.prices.infrastructure.adapter.out.persistence.repository.JpaPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PriceRepositoryAdapter implements PriceRepository {

    private final JpaPriceRepository jpaPriceRepository;
    private final PriceEntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Price> findApplicablePrice(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId
    ) {
        return jpaPriceRepository
                .findTopByProductIdAndBrandIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        productId,
                        brandId,
                        applicationDate,
                        applicationDate
                )
                .map(mapper::toDomain);
    }
}
