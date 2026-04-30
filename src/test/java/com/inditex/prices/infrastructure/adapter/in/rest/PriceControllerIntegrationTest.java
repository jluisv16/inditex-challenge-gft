package com.inditex.prices.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    private static final String URL = "/api/v1/prices";
    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test 1 - 14 Jun 10:00 → price list 1, price 35.50")
    void test1_june14At10h_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-14T10:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("Test 2 - 14 Jun 16:00 → price list 2, price 25.45 (higher priority overlap)")
    void test2_june14At16h_shouldReturnPriceList2() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-14T16:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.price").value(25.45));
    }

    @Test
    @DisplayName("Test 3 - 14 Jun 21:00 → price list 1, price 35.50")
    void test3_june14At21h_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-14T21:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    @DisplayName("Test 4 - 15 Jun 10:00 → price list 3, price 30.50")
    void test4_june15At10h_shouldReturnPriceList3() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-15T10:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(3))
                .andExpect(jsonPath("$.price").value(30.50));
    }

    @Test
    @DisplayName("Test 5 - 16 Jun 21:00 → price list 4, price 38.95")
    void test5_june16At21h_shouldReturnPriceList4() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-16T21:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(4))
                .andExpect(jsonPath("$.price").value(38.95));
    }

    @Test
    @DisplayName("404 - No price found for a date outside all ranges")
    void whenNoPriceExists_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2019-01-01T00:00:00Z")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Price Not Found"));
    }

    @Test
    @DisplayName("400 - Missing required parameter applicationDate")
    void whenMissingParam_shouldReturn400() throws Exception {
        mockMvc.perform(get(URL)
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
