package ru.itis.parser.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.service.HotelService;

import jakarta.ws.rs.core.Response;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class HotelRestControllerTest {

    @InjectMock
    HotelService hotelService;

    @Test
    @DisplayName("Endpoint: GET /api/v1/hotels should call hotelService with all parameters, because they all provided")
    void testFindAllHotelsByFiltersAllParametersProvided() {
        // Arrange
        String name = "name";
        Integer countOfStarsFrom = 3;
        Integer countOfStarsTo = 4;
        Integer priceFrom = 100;
        Integer priceTo = 500;
        Double ratingFrom = 8.4;
        Double ratingTo = 10.0;
        String cityName = "city";
        String countryName = "country";
        FindAllHotelsParameters findAllHotelsParameters = new FindAllHotelsParameters(name, countOfStarsFrom, countOfStarsTo, priceFrom, priceTo, ratingFrom, ratingTo, cityName, countryName);

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel A");
        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel B");
        List<Hotel> expectedHotels = List.of(hotel1, hotel2);

        when(hotelService.findAllHotelsByFilters(findAllHotelsParameters))
                .thenReturn(expectedHotels);

        // Act & Assert
        given()
                .queryParam("name", name)
                .queryParam("count-stars-from", countOfStarsFrom)
                .queryParam("count-stars-to", countOfStarsTo)
                .queryParam("min-price-from", priceFrom)
                .queryParam("min-price-to", priceTo)
                .queryParam("rating-from", ratingFrom)
                .queryParam("rating-to", ratingTo)
                .queryParam("city", cityName)
                .queryParam("country", countryName)
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
    @DisplayName("Endpoint: GET /api/v1/hotels should call hotelService with default parameters, because they are not provided")
    void testFindAllHotelsByFiltersParametersAreNotProvided() {
        // Arrange
        String name = null;
        Integer countOfStarsFrom = 0;
        Integer countOfStarsTo = 5;
        Integer priceFrom = 0;
        Integer priceTo = 2147483647;
        Double ratingFrom = 0.0;
        Double ratingTo = 10.0;
        String cityName = null;
        String countryName = null;
        FindAllHotelsParameters findAllHotelsParameters = new FindAllHotelsParameters(name, countOfStarsFrom, countOfStarsTo, priceFrom, priceTo, ratingFrom, ratingTo, cityName, countryName);

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel A");
        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel B");
        List<Hotel> expectedHotels = List.of(hotel1, hotel2);

        when(hotelService.findAllHotelsByFilters(findAllHotelsParameters))
                .thenReturn(expectedHotels);

        // Act & Assert
        given()
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
    @DisplayName("Endpoint: GET /api/v1/hotels should return 404, because parameters are invalid")
    void testFindAllHotelsByFiltersInvalidParameter() {
        // Arrange
        // Act & Assert
        given()
                .queryParam("rating-from", "word")
                .when()
                .get("/api/v1/hotels")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}