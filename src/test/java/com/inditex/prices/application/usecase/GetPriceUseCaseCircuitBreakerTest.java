package com.inditex.prices.application.usecase;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.infrastructure.exception.PriceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "resilience4j.circuitbreaker.instances.priceService.slidingWindowSize=3",
        "resilience4j.circuitbreaker.instances.priceService.minimumNumberOfCalls=3",
        "resilience4j.circuitbreaker.instances.priceService.failureRateThreshold=50"
})
@ActiveProfiles("test")
class GetPriceUseCaseCircuitBreakerTest {

    @Autowired
    private GetPriceUseCase useCase;

    @MockitoBean
    private PriceRepository priceRepository;

    private static final Instant DATE = Instant.parse("2020-06-14T10:00:00Z");

    @Test
    void circuitBreaker_shouldOpenAfterFailures() {

        when(priceRepository.findApplicablePrice(any(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("DB down"));

        for (int i = 0; i < 5; i++) {
            try {
                useCase.getApplicablePrice(DATE, 35455L, 1L);
            } catch (Exception ignored) {}
        }

        assertThrows(PriceNotFoundException.class,
                () -> useCase.getApplicablePrice(DATE, 35455L, 1L));
    }
}
