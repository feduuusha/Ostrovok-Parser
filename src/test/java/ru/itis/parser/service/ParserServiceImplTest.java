package ru.itis.parser.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.web.client.WebClient;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itis.parser.client.CitiesRestClient;
import ru.itis.parser.model.MultiComplete;
import ru.itis.parser.model.Region;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ParserServiceImplTest {

    @InjectMock
    @RestClient
    CitiesRestClient citiesRestClient;

    @InjectMock
    WebClient client;

    @InjectMock
    HotelService hotelService;

    @ConfigProperty(name = "ostrovok.hotels.uri.pattern")
    String uriPattern;

    ParserServiceImpl parserService;

    @BeforeEach
    void setUp() {
        parserService = new ParserServiceImpl(citiesRestClient, client, hotelService);
    }

    @Test
    void testParseHotelsByCity_CityNotFound() {
        // Arrange
        String queryCity = "UnknownCity";
        MultiComplete multiComplete = new MultiComplete();
        multiComplete.setRegions(new ArrayList<>());
        when(citiesRestClient.searchCities(queryCity)).thenReturn(multiComplete);

        // Act & Assert
        assertThatThrownBy(() -> parserService.parseHotelsByCity(queryCity))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("City with name: " + queryCity + " not found");

        verify(citiesRestClient).searchCities(queryCity);
        verifyNoInteractions(client, hotelService);
    }

    @Test
    void testParseHotelsByCity_InternalServerError() {
        // Arrange
        String queryCity = "Moscow";
        Region region = new Region();
        region.setCountry("Russia");
        region.setName("Moscow");
        region.setSlug("moscow");
        region.setType("City");
        MultiComplete multiComplete = new MultiComplete();
        multiComplete.setRegions(List.of(region));
        parserService.uriPattern = uriPattern;


        when(citiesRestClient.searchCities(queryCity)).thenReturn(multiComplete);

        when(client.getAbs(anyString()))
                .thenThrow(new InternalServerErrorException("Internal server error"));

        // Act & Assert
        assertThatThrownBy(() -> parserService.parseHotelsByCity(queryCity))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Internal server error");

        verify(citiesRestClient).searchCities(queryCity);
        verify(client).getAbs(String.format(uriPattern, "moscow", 1));
        verifyNoInteractions(hotelService);
    }

}