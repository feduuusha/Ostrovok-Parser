package ru.itis.parser.exception;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.itis.parser.service.ParserService;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;

@QuarkusTest
public class ExceptionMappersTest {

    @InjectMock
    ParserService parserService;

    @ParameterizedTest
    @MethodSource("provideExceptionsAndExpectedResponses")
    void testParseHotelsByCityServiceThrowsException(Exception exception, int expectedStatusCode, String expectedMessage) {
        // Arrange
        String city = "Moscow";
        doThrow(exception)
                .when(parserService)
                .parseHotelsByCity(city);

        // Act & Assert
        given()
                .queryParam("city", city)
                .when()
                .post("/api/v1/parse/hotels")
                .then()
                .statusCode(expectedStatusCode)
                .body(equalTo(expectedMessage));
    }

    private static Stream<Arguments> provideExceptionsAndExpectedResponses() {
        return Stream.of(
                Arguments.of(new NotFoundException("Not Found"), 404, "Not Found"),
                Arguments.of(new BadRequestException("Bad Request"), 400, "Bad Request"),
                Arguments.of(new InternalServerErrorException("Internal Server Error"), 500, "Internal Server Error"),
                Arguments.of(new IllegalArgumentException("Invalid input"), 500, "Unexpected error on server")
        );
    }
}