package com.inditex.prices.infrastructure.adapter.out.persistence.repository;


import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            LocalDateTime start,
            LocalDateTime end,
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
        LocalDateTime queryDate = LocalDateTime.of(2020, 6, 14, 10, 0);

        repository.save(buildEntity(
                1,
                0,
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59),
                new BigDecimal("35.50")
        ));

        repository.save(buildEntity(
                2,
                1,
                LocalDateTime.of(2020, 6, 14, 9, 0),
                LocalDateTime.of(2020, 6, 14, 18, 0),
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
        LocalDateTime queryDate = LocalDateTime.of(1999, 1, 1, 0, 0);

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