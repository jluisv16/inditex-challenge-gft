package com.inditex.prices.cache;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.application.usecase.GetPriceUseCase;
import com.inditex.prices.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GetPriceUseCaseCacheTest {

    @Autowired
    private GetPriceUseCase useCase;

    @MockitoBean
    private PriceRepository priceRepository;

    @Autowired
    private CacheManager cacheManager;

    private static final Instant DATE = Instant.parse("2020-06-14T10:00:00Z");

    @BeforeEach
    void setUp() {
        var cache = cacheManager.getCache("prices");
        if (cache != null) cache.clear();
    }

    @Test
    void cache_shouldReturnSameResultWithoutCallingRepositoryMultipleTimes() {

        Price price = new Price(
                35455L,
                1L,
                1,
                DATE,
                DATE,
                new BigDecimal("35.50"),
                "EUR"
        );

        when(priceRepository.findApplicablePrice(any(), anyLong(), anyLong()))
                .thenReturn(java.util.Optional.of(price));

        // 🔁 múltiples llamadas
        Price r1 = useCase.getApplicablePrice(DATE, 35455L, 1L);
        Price r2 = useCase.getApplicablePrice(DATE, 35455L, 1L);
        Price r3 = useCase.getApplicablePrice(DATE, 35455L, 1L);

        // ✔️ mismo resultado
        assertEquals(r1, r2);
        assertEquals(r2, r3);

        // 🔥 clave del test
        verify(priceRepository, times(1))
                .findApplicablePrice(any(), anyLong(), anyLong());
    }

    @Test
    void cache_shouldBeClearedBetweenDifferentKeys() {

        Price price = buildPrice();

        when(priceRepository.findApplicablePrice(any(), anyLong(), anyLong()))
                .thenReturn(java.util.Optional.of(price));

        useCase.getApplicablePrice(DATE, 35455L, 1L);
        useCase.getApplicablePrice(DATE, 35456L, 1L); // diferente productId

        verify(priceRepository, times(2))
                .findApplicablePrice(any(), anyLong(), anyLong());
    }

    private Price buildPrice() {
        return new Price(
                35455L,
                1L,
                1,
                Instant.parse("2020-06-14T00:00:00Z"),
                Instant.parse("2020-12-31T23:59:59Z"),
                new BigDecimal("35.50"),
                "EUR"
        );
    }
}