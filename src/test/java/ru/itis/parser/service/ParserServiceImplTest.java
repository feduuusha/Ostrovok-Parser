package ru.itis.parser.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.itis.parser.client.CitiesRestClient;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.MultiComplete;
import ru.itis.parser.model.Region;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** @noinspection unchecked*/
@QuarkusTest
public class ParserServiceImplTest {

    @InjectMock
    @RestClient
    CitiesRestClient citiesRestClient;

    @InjectMock
    WebClient client;

    @InjectMock
    HotelService hotelService;

    @Inject
    ParserServiceImpl parserService;

    @BeforeEach
    void setUp() {
        parserService.uriPattern = "uri-patterns%s%s";
        parserService.hotelRatingPrefix = "TotalRating_content";
        parserService.acceptLanguage = "ru";
        parserService.cookieValue = "cookie=1";
        parserService.hotelCardsPrefix = "HotelListDateless_card";
        parserService.hotelAddressPrefix = "HotelCard_address";
        parserService.hotelCountOfStartPrefix = "Stars_star_";
        parserService.hotelPricePrefix = "HotelCard_ratePriceValue";
        parserService.hotelTitlePrefix = "HotelCard_title";
        parserService.paginationClassPrefix = "Pagination_item";
    }

    @Test
    @DisplayName("Method: parseHotelsByCity(...) should throw NotFoundException, because city with provided name not found")
    void testParseHotelsByCity_CityNotFound() {
        // Arrange
        String queryCity = "Leningrad";
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
    @DisplayName("Method: parseHotelsByCity(...) should call hotelServiceMethod with list of hotels")
    void testParseHotelsByCity_Correct() throws FileNotFoundException {
        // Arrange
        String queryCity = "Orenburg";
        MultiComplete multiComplete = new MultiComplete();
        List<Region> regions = new ArrayList<>();
        HttpRequest<Buffer> request = Mockito.mock(HttpRequest.class);
        HttpRequest<String> stringRequest = Mockito.mock(HttpRequest.class);
        Future<HttpResponse<String>> future = Mockito.mock(Future.class);
        HttpResponse<String> response = Mockito.spy(HttpResponse.class);
        Region region = new Region();
        region.setType("City");
        region.setSlug("russia/orenburg");
        region.setCountry("Russia");
        regions.add(region);
        List<Hotel> hotels = new ArrayList<>();
        int countOfPages = 28;
        Hotel hotel = new Hotel(null, 7389099L, "Отель OTO Tweed", "улица Самолетная , д. 194, Оренбург", 3, 2150, 8.4, "/hotel/russia/orenburg/mid7389099/tarik/", null);
        for (int i = 0; i < countOfPages; ++i) {
            hotels.add(hotel);
        }
        String html = String.join("", new BufferedReader(new FileReader("src/test/resources/hotels-page-example.html")).lines().toList());
        multiComplete.setRegions(regions);
        when(citiesRestClient.searchCities(queryCity)).thenReturn(multiComplete);
        when(client.getAbs(any(String.class))).thenReturn(request);
        when(request.putHeader(any(), any(String.class))).thenReturn(request);
        when(request.as(BodyCodec.string())).thenReturn(stringRequest);
        when(stringRequest.send()).thenReturn(future);
        when(future.toCompletionStage()).thenReturn(CompletableFuture.completedFuture(response));
        when(response.body()).thenReturn(html);

        // Act
        parserService.parseHotelsByCity(queryCity);

        // Assert
        verify(citiesRestClient).searchCities(queryCity);
        verify(client, times(countOfPages)).getAbs(any(String.class));
        verify(hotelService).upsertHotels(hotels, region);
    }

    @Test
    @DisplayName("Method: parseHotelsByCity(...) should throw InternalServerError, because incorrect html structure")
    void testParseHotelsByCity_IncorrectHtmlStructure() throws FileNotFoundException {
        // Arrange
        String queryCity = "Orenburg";
        MultiComplete multiComplete = new MultiComplete();
        List<Region> regions = new ArrayList<>();
        HttpRequest<Buffer> request = Mockito.mock(HttpRequest.class);
        HttpRequest<String> stringRequest = Mockito.mock(HttpRequest.class);
        Future<HttpResponse<String>> future = Mockito.mock(Future.class);
        HttpResponse<String> response = Mockito.spy(HttpResponse.class);
        Region region = new Region();
        region.setType("City");
        region.setSlug("russia/orenburg");
        region.setCountry("Russia");
        regions.add(region);
        String html = String.join("", new BufferedReader(new FileReader("src/test/resources/hotels-page-example.html")).lines().toList());
        html = html.replaceAll("mid[0-9]*", "");
        multiComplete.setRegions(regions);
        when(citiesRestClient.searchCities(queryCity)).thenReturn(multiComplete);
        when(client.getAbs(any(String.class))).thenReturn(request);
        when(request.putHeader(any(), any(String.class))).thenReturn(request);
        when(request.as(BodyCodec.string())).thenReturn(stringRequest);
        when(stringRequest.send()).thenReturn(future);
        when(future.toCompletionStage()).thenReturn(CompletableFuture.completedFuture(response));
        when(response.body()).thenReturn(html);

        // Act
        // Assert
        assertThatExceptionOfType(InternalServerErrorException.class)
                .isThrownBy(() -> parserService.parseHotelsByCity(queryCity))
                .withMessage("Hotel id not found on page");

        // Assert
        verify(citiesRestClient).searchCities(queryCity);
        verify(client, times(1)).getAbs(any(String.class));
        verify(hotelService, never()).upsertHotels(any(), any());
    }
}