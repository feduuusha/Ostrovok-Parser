package ru.itis.parser.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.City;
import ru.itis.parser.entity.Country;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.Region;
import ru.itis.parser.repository.CityRepository;
import ru.itis.parser.repository.CountryRepository;
import ru.itis.parser.repository.HotelRepository;

import jakarta.transaction.Transactional;
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

    HotelServiceImpl hotelService;

    @BeforeEach
    void setUp() {
        hotelService = new HotelServiceImpl(countryRepository, cityRepository, hotelRepository);
    }

    @Test
    @Transactional
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
    @Transactional
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
    void testFindAllHotelsByFilters_WithCityName() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, "Moscow", null);
        City city = new City(1L, "Moscow", 1L, "moscow");
        Hotel hotel = new Hotel();
        hotel.setCityId(1L);
        hotel.setName("Hotel A");

        when(cityRepository.findByName("Moscow")).thenReturn(List.of(city));
        when(hotelRepository.findAllByFilters(parameters)).thenReturn(List.of(hotel));

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hotel A");
    }

    @Test
    void testFindAllHotelsByFilters_WithCountryName() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, null, "Russia");
        Country country = new Country(1L, "Russia");
        City city = new City(1L, "Moscow", 1L, "moscow");
        Hotel hotel = new Hotel();
        hotel.setCityId(1L);
        hotel.setName("Hotel A");

        when(cityRepository.findAll()).thenReturn(List.of(city));
        when(countryRepository.findByName("Russia")).thenReturn(Optional.of(country));
        when(hotelRepository.findAllByFilters(parameters)).thenReturn(List.of(hotel));

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hotel A");
    }

    @Test
    void testFindAllHotelsByFilters_NoResults() {
        // Arrange
        FindAllHotelsParameters parameters = new FindAllHotelsParameters(null, null, null, null, null, null, null, "UnknownCity", null);
        when(cityRepository.findByName("UnknownCity")).thenReturn(List.of());

        // Act
        List<Hotel> result = hotelService.findAllHotelsByFilters(parameters);

        // Assert
        assertThat(result).isEmpty();
    }
}