package ru.itis.parser.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.service.HotelService;

import jakarta.ws.rs.core.Response;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
public class HotelRestControllerTest {

    @InjectMock
    HotelService hotelService;

    @BeforeEach
    void setUp() {
        Mockito.reset(hotelService);
    }

    @Test
    void testFindAllHotelsByFiltersSuccess() {
        // Arrange
        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel A");

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel B");

        List<Hotel> expectedHotels = List.of(hotel1, hotel2);

        when(hotelService.findAllHotelsByFilters(any(FindAllHotelsParameters.class)))
                .thenReturn(expectedHotels);

        // Act & Assert
        given()
                .queryParam("city", "Moscow")
                .queryParam("minPrice", 100)
                .queryParam("maxPrice", 500)
                .when()
                .get("/api/v1/hotels")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("[0].id", equalTo(1))
                .body("[0].name", equalTo("Hotel A"))
                .body("[1].id", equalTo(2))
                .body("[1].name", equalTo("Hotel B"));
    }

    @Test
    void testFindAllHotelsByFiltersInvalidParameters() {
        // Arrange
        when(hotelService.findAllHotelsByFilters(any(FindAllHotelsParameters.class)))
                .thenThrow(new BadRequestException("Invalid parameters"));

        // Act & Assert
        given()
                .queryParam("minPrice", -1)
                .when()
                .get("/api/v1/hotels")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}