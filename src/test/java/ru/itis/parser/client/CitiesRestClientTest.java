package ru.itis.parser.client;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.itis.parser.model.MultiComplete;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;
import ru.itis.parser.model.Region;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
public class CitiesRestClientTest {

    @InjectMock
    @RestClient
    CitiesRestClient citiesRestClient;

    @BeforeEach
    void setUp() {
        Mockito.reset(citiesRestClient);
    }

    @Test
    void testSearchCitiesSuccess() {
        // Arrange
        MultiComplete expectedResponse = new MultiComplete();
        expectedResponse.setRegions(List.of(new Region(), new Region()));
        Mockito.when(citiesRestClient.searchCities(anyString())).thenReturn(expectedResponse);

        // Act
        MultiComplete actualResponse = citiesRestClient.searchCities("Moscow");

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testSearchCitiesBadRequest() {
        // Arrange
        Mockito.when(citiesRestClient.searchCities(anyString()))
                .thenThrow(new BadRequestException("The cities client responded with HTTP 404"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> citiesRestClient.searchCities("UnknownCity"));
    }

    @Test
    void testSearchCitiesNotFound() {
        // Arrange
        Mockito.when(citiesRestClient.searchCities(anyString()))
                .thenThrow(new NotFoundException("The cities client responded with HTTP 404"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> citiesRestClient.searchCities("UnknownCity"));
    }

    @Test
    void testSearchCitiesServerError() {
        // Arrange
        Mockito.when(citiesRestClient.searchCities(anyString()))
                .thenThrow(new ServerErrorException("The cities client responded with server error", Response.Status.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(ServerErrorException.class, () -> citiesRestClient.searchCities("Moscow"));
    }

    @Test
    void testSearchCitiesClientError() {
        // Arrange
        Mockito.when(citiesRestClient.searchCities(anyString()))
                .thenThrow(new ClientErrorException("The cities client responded with client error", Response.Status.BAD_REQUEST));

        // Act & Assert
        assertThrows(ClientErrorException.class, () -> citiesRestClient.searchCities("Moscow"));
    }
}