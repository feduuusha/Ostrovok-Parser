package ru.itis.parser.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.itis.parser.service.ParserService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class ParserRestControllerTest {

    @InjectMock
    ParserService parserService;

    @BeforeEach
    void setUp() {
        Mockito.reset(parserService);
    }

    @Test
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
    void testParseHotelsByCityMissingCityParameter() {
        // Act & Assert
        given()
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(400)
                .body(equalTo("City is required parameter"));
    }

    @Test
    void testParseHotelsByCityBlankCityParameter() {
        // Act & Assert
        given()
                .queryParam("city", "   ")
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(400)
                .body(equalTo("City is required parameter"));
    }

    @Test
    void testParseHotelsByCityServiceThrowsException() {
        // Arrange
        String city = "Moscow";
        doThrow(new RuntimeException("Service error"))
                .when(parserService)
                .parseHotelsByCity(anyString());

        // Act & Assert
        given()
                .queryParam("city", city)
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(500);
    }
}