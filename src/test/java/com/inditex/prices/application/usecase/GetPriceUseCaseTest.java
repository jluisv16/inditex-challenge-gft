package com.inditex.prices.application.usecase;

import com.inditex.prices.application.port.out.PriceRepository;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.exception.PriceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPriceUseCaseTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private GetPriceUseCase useCase;

    private static final LocalDateTime DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    private static final LocalDateTime END_DATE = DATE.plusDays(1);

    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    @Test
    void whenPriceExists_shouldReturnIt() {

        Price expected = new Price(
                PRODUCT_ID,
                BRAND_ID,
                1,
                DATE,
                END_DATE,
                BigDecimal.valueOf(35.50),
                "EUR"
        );

        when(priceRepository.findApplicablePrice(DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(Optional.of(expected));

        Price result = useCase.getApplicablePrice(DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenNoPriceExists_shouldThrowPriceNotFoundException() {

        when(priceRepository.findApplicablePrice(DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.getApplicablePrice(DATE, PRODUCT_ID, BRAND_ID)
        )
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining(PRODUCT_ID.toString());
    }
}