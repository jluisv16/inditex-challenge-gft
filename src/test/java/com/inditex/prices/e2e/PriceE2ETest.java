package com.inditex.prices.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "resilience4j.circuitbreaker.instances.priceService.enabled=false",
                "resilience4j.retry.instances.priceService.enabled=false",
                "resilience4j.bulkhead.instances.priceService.enabled=false",
                "spring.cache.type=none"
        }
)
@ActiveProfiles("test")
class PriceE2ETest {

    @LocalServerPort
    private int port;

    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/prices";
    }

    @Test
    @DisplayName("E2E Test 1 - 14 Jun 10:00 UTC → price list 1, price 35.50")
    void e2e_test1_june14At10h_shouldReturnPriceList1() {
        given()
            .queryParam("applicationDate", "2020-06-14T10:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("productId", equalTo(35455))
            .body("brandId", equalTo(1))
            .body("priceList", equalTo(1))
            .body("price", equalTo(35.50f))
            .body("currency", equalTo("EUR"));
    }

    @Test
    @DisplayName("E2E Test 2 - 14 Jun 16:00 UTC → price list 2, price 25.45 (higher priority)")
    void e2e_test2_june14At16h_shouldReturnPriceList2() {
        given()
            .queryParam("applicationDate", "2020-06-14T16:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(2))
            .body("price", equalTo(25.45f));
    }

    @Test
    @DisplayName("E2E Test 3 - 14 Jun 21:00 UTC → price list 1, price 35.50")
    void e2e_test3_june14At21h_shouldReturnPriceList1() {
        given()
            .queryParam("applicationDate", "2020-06-14T21:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(1))
            .body("price", equalTo(35.50f));
    }

    @Test
    @DisplayName("E2E Test 4 - 15 Jun 10:00 UTC → price list 3, price 30.50")
    void e2e_test4_june15At10h_shouldReturnPriceList3() {
        given()
            .queryParam("applicationDate", "2020-06-15T10:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(3))
            .body("price", equalTo(30.50f));
    }

    @Test
    @DisplayName("E2E Test 5 - 16 Jun 21:00 UTC → price list 4, price 38.95")
    void e2e_test5_june16At21h_shouldReturnPriceList4() {
        given()
            .queryParam("applicationDate", "2020-06-16T21:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(4))
            .body("price", equalTo(38.95f));
    }

    @Test
    @DisplayName("E2E - 404 when no price exists for date")
    void e2e_whenNoPriceExists_shouldReturn404() {
        given()
            .queryParam("applicationDate", "2019-01-01T00:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(404)
            .body("title", equalTo("Price Not Found"));
    }

    @Test
    @DisplayName("E2E - 400 when missing required parameter")
    void e2e_whenMissingParam_shouldReturn400() {
        given()
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(400);
    }
    
    @Test
    @DisplayName("E2E - Timezone handling: same instant in different timezones returns same price")
    void e2e_timezoneHandling_sameInstantDifferentTimezones() {

        given()
            .queryParam("applicationDate", "2020-06-14T10:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(1))
            .body("price", equalTo(35.50f));
        
        // Request en Madrid timezone (mismo instante)
        given()
            .queryParam("applicationDate", "2020-06-14T12:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(1))
            .body("price", equalTo(35.50f));
    }
    
    @Test
    @DisplayName("E2E - Edge case: timezone affects priority selection at boundary")
    void e2e_timezoneAtBoundary() {

        given()
            .queryParam("applicationDate", "2020-06-14T15:00:00+02:00")
            .queryParam("productId", PRODUCT_ID)
            .queryParam("brandId", BRAND_ID)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("priceList", equalTo(2));
    }
}
