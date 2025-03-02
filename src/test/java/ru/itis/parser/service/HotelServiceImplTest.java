package ru.itis.parser.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.City;
import ru.itis.parser.entity.Country;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.Region;
import ru.itis.parser.repository.CityRepository;
import ru.itis.parser.repository.CountryRepository;
import ru.itis.parser.repository.HotelRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
public class HotelServiceImplTest {

    @InjectMock
    CountryRepository countryRepository;

    @InjectMock
    CityRepository cityRepository;

    @InjectMock
    HotelRepository hotelRepository;

    @Inject
    HotelServiceImpl hotelService;

    @Test
    @DisplayName("Method: upsertHotels(...) should save new country and new city and insert hotels, because they are new")
    void testUpsertHotels_NewCountryAndCity() {
        // Arrange
        Region region = new Region();
        region.setCountry("Russia");
        region.setName("Moscow");
        region.setSlug("moscow");
        Hotel hotel = new Hotel();
        hotel.setName("Hotel A");

        when(countryRepository.findByName("Russia")).thenReturn(Optional.empty());
        when(cityRepository.findByNameAndCountryId(eq("Moscow"), any())).thenReturn(Optional.empty());
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> {
            Country country = invocation.getArgument(0);
            country.setId(1L);
            return country;
        });
        when(cityRepository.save(any(City.class))).thenAnswer(invocation -> {
            City city = invocation.getArgument(0);
            city.setId(1L);
            return city;
        });

        // Act
        hotelService.upsertHotels(List.of(hotel), region);

        // Assert
        verify(countryRepository).findByName("Russia");
        verify(cityRepository).findByNameAndCountryId("Moscow", 1L);
        verify(countryRepository).save(any(Country.class));
        verify(cityRepository).save(any(City.class));
        verify(hotelRepository).upsert(List.of(hotel));
        assertThat(hotel.getCityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Method: upsertHotels(...) should do not save new country and new city, because they are exist, and should upsert hotels")
    void testUpsertHotels_ExistingCountryAndCity() {
        // Arrange
        Region region = new Region();
        region.setCountry("Russia");
        region.setName("Moscow");
        region.setSlug("moscow");
        Hotel hotel = new Hotel();
        hotel.setName("Hotel A");

        Country existingCountry = new Country(1L, "Russia");
        City existingCity = new City(1L, "Moscow", 1L, "moscow");

        when(countryRepository.findByName("Russia")).thenReturn(Optional.of(existingCountry));
        when(cityRepository.findByNameAndCountryId("Moscow", 1L)).thenReturn(Optional.of(existingCity));

        // Act
        hotelService.upsertHotels(List.of(hotel), region);

        // Assert
        verify(countryRepository).findByName("Russia");
        verify(cityRepository).findByNameAndCountryId("Moscow", 1L);
        verify(countryRepository, never()).save(any(Country.class));
        verify(cityRepository, never()).save(any(City.class));
        verify(hotelRepository).upsert(List.of(hotel));
        assertThat(hotel.getCityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Method: findAllHotelsByFilters(...) should return all hotels, because all filter are null")
    void testFindAllHotelsByFilters_AllFiltersNull() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, null, null);
        List<Hotel> hotels = List.of(new Hotel(), new Hotel());
        List<City> cities = List.of(new City(1L, "Kazan", 2L, "russia/kazan"));
        hotels.get(0).setCityId(1L);
        hotels.get(0).setName("Hotel A");
        hotels.get(1).setCityId(100L);
        hotels.get(1).setName("Hotel B");

        when(cityRepository.findAll()).thenReturn(cities);
        when(hotelRepository.findAllByFilters(parameters)).thenReturn(hotels);

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        verify(cityRepository).findAll();
        verify(countryRepository, never()).findByName(any());
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result).hasSize(1);
        softly.assertThat(result.get(0).getName()).isEqualTo("Hotel A");

        softly.assertAll();
    }

    @Test
    @DisplayName("Method: findAllHotelsByFilters(...) should return hotels by cityName and countryName, because they are provided")
    void testFindAllHotelsByFilters_ByCityAndCountry() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, "Orenburg", "Russia");
        List<Hotel> hotels = List.of(new Hotel(), new Hotel());
        Country country = new Country(77L, "Russia");
        List<City> cities = List.of(
                new City(1L, "Orenburg", 77L, "russia/orenburg"),
                new City(2L, "Orenburg", 100L, "german/orenburg")
        );
        hotels.get(0).setCityId(1L);
        hotels.get(0).setName("Hotel A");
        hotels.get(1).setCityId(2L);
        hotels.get(1).setName("Hotel B");

        when(cityRepository.findByName("Orenburg")).thenReturn(cities);
        when(countryRepository.findByName("Russia")).thenReturn(Optional.of(country));
        when(hotelRepository.findAllByFilters(parameters)).thenReturn(hotels);

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        verify(cityRepository).findByName("Orenburg");
        verify(countryRepository).findByName("Russia");
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result).hasSize(1);
        softly.assertThat(result.get(0).getName()).isEqualTo("Hotel A");

        softly.assertAll();
    }

    @Test
    @DisplayName("Method: findAllHotelsByFilters(...) should return empty list, because country with provided name do not exist")
    void testFindAllHotelsByFilters_NoResults() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, null, "Rossiiskaia Imperiia");
        when(cityRepository.findAll()).thenReturn(List.of());
        when(countryRepository.findByName("Rossiiskaia Imperiia")).thenReturn(Optional.empty());

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        verify(countryRepository).findByName("Rossiiskaia Imperiia");
        assertThat(result).isEmpty();
    }
}