package ru.itis.parser.client;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itis.parser.model.MultiComplete;

import jakarta.inject.Inject;
import ru.itis.parser.model.Region;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@QuarkusTest
@Testcontainers(disabledWithoutDocker = true)
@QuarkusTestResource(CitiesRestClientResource.class)
public class CitiesRestClientTest {

    @Inject
    @RestClient
    CitiesRestClient citiesRestClient;

    @Test
    @DisplayName("Client should return correct answer, because request is correct")
    void testSearchCities_Success() {
        // Arrange
        String cityName = "Sydney";
        MultiComplete multiComplete = new MultiComplete();
        Region region = new Region();
        region.setName("Сидней, Новый Южный Уэльс");
        region.setType("City");
        region.setSlug("australia/sydney");
        region.setCountry("Австралия");
        multiComplete.setRegions(List.of(region));

        // Act
        MultiComplete result = citiesRestClient.searchCities(cityName);

        // Assert
        assertThat(result).isEqualTo(multiComplete);
    }

    @ParameterizedTest
    @MethodSource("provideCityNameAndExpectedResponses")
    void testParseHotelsByCityServiceThrowsException(String query, Class<Exception> expectedExceptionType, String expectedMessage) {
        // Act & Assert
        assertThatExceptionOfType(expectedExceptionType)
                .isThrownBy(() -> citiesRestClient.searchCities(query))
                .withMessage(expectedMessage);
    }

    private static Stream<Arguments> provideCityNameAndExpectedResponses() {
        return Stream.of(
                Arguments.of("500", ServerErrorException.class, "The cities client responded with server error"),
                Arguments.of("400", BadRequestException.class, "The cities client responded with HTTP 400"),
                Arguments.of("404", NotFoundException.class, "The cities client responded with HTTP 404"),
                Arguments.of("403", ClientErrorException.class, "The cities client responded with client error")
        );
    }
}