package com.inditex.prices.application.usecase;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPriceUseCaseResilienceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private GetPriceUseCase useCase;

    @Test
    void retry_shouldRetryAndFail() {

        when(priceRepository.findApplicablePrice(any(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> useCase.getApplicablePrice(
                        Instant.parse("2020-06-14T10:00:00Z"),
                        35455L,
                        1L
                ));

        verify(priceRepository, times(1))
                .findApplicablePrice(any(), anyLong(), anyLong());
    }

    @Test
    void retry_shouldSucceedOnSecondAttempt() {

        Price expected = new Price(
                35455L,
                1L,
                1,
                Instant.parse("2020-06-14T00:00:00Z"),
                Instant.parse("2020-12-31T23:59:59Z"),
                new BigDecimal("35.50"),
                "EUR"
        );

        when(priceRepository.findApplicablePrice(any(), anyLong(), anyLong()))
                .thenReturn(Optional.of(expected));

        Price result = useCase.getApplicablePrice(
                Instant.parse("2020-06-14T10:00:00Z"),
                35455L,
                1L
        );

        assertEquals(expected.price(), result.price());

        verify(priceRepository, times(1))
                .findApplicablePrice(any(), anyLong(), anyLong());
    }
}