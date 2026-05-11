package com.inditex.prices.infrastructure.adapter.out.persistence.repository;

import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaPriceRepositoryTest {

    @Autowired
    private JpaPriceRepository repository;

    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    private PriceEntity buildEntity(
            Integer priceList,
            int priority,
            Instant start,
            Instant end,
            BigDecimal price
    ) {
        PriceEntity entity = new PriceEntity();
        entity.setProductId(PRODUCT_ID);
        entity.setBrandId(BRAND_ID);
        entity.setPriceList(priceList);
        entity.setPriority(priority);
        entity.setStartDate(start);
        entity.setEndDate(end);
        entity.setPrice(price);
        entity.setCurrency("EUR");
        return entity;
    }

    @Test
    void shouldReturnPriceWithHighestPriority() {

        // given
        Instant queryDate = OffsetDateTime.of(
                2020, 6, 14, 10, 0, 0, 0, ZoneOffset.UTC
        ).toInstant();

        repository.save(buildEntity(
                1,
                0,
                OffsetDateTime.of(2020, 6, 14, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                OffsetDateTime.of(2020, 12, 31, 23, 59, 0, 0, ZoneOffset.UTC).toInstant(),
                new BigDecimal("35.50")
        ));

        repository.save(buildEntity(
                2,
                1,
                OffsetDateTime.of(2020, 6, 14, 9, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                OffsetDateTime.of(2020, 6, 14, 18, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                new BigDecimal("25.45")
        ));

        // when
        Optional<PriceEntity> result =
                repository.findTopByProductIdAndBrandIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        PRODUCT_ID,
                        BRAND_ID,
                        queryDate,
                        queryDate
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualByComparingTo("25.45");
        assertThat(result.get().getPriceList()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyWhenNoPriceExists() {

        // given
        Instant queryDate = Instant.parse("1999-01-01T00:00:00Z");

        // when
        Optional<PriceEntity> result =
                repository.findTopByProductIdAndBrandIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        PRODUCT_ID,
                        BRAND_ID,
                        queryDate,
                        queryDate
                );

        // then
        assertThat(result).isEmpty();
    }
}