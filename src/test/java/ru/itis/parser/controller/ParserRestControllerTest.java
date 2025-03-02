package ru.itis.parser.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itis.parser.service.ParserService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ParserRestControllerTest {

    @InjectMock
    ParserService parserService;

    @Test
    @DisplayName("Endpoint: POST /api/v1/parse/hotels should call parserService.parseHotelsByCity(), because city parameter provided")
    void testParseHotelsByCitySuccess() {
        // Arrange
        String city = "Moscow";

        // Act & Assert
        given()
                .queryParam("city", city)
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(204);

        verify(parserService).parseHotelsByCity(city);
    }

    @Test
    @DisplayName("Endpoint: POST /api/v1/parse/hotels should return 400, because city parameter is not provided")
    void testParseHotelsByCityMissingCityParameter() {
        // Act & Assert
        given()
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(400)
                .body(equalTo("City is required parameter"));
        verify(parserService, never()).parseHotelsByCity(any());
    }

    @Test
    @DisplayName("Endpoint: POST /api/v1/parse/hotels should return 400, because city parameter is blank")
    void testParseHotelsByCityBlankCityParameter() {
        // Act & Assert
        given()
                .queryParam("city", "   ")
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(400)
                .body(equalTo("City is required parameter"));
        verify(parserService, never()).parseHotelsByCity(any());
    }

}