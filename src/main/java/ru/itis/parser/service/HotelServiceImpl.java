package ru.itis.parser.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.City;
import ru.itis.parser.entity.Country;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.Region;
import ru.itis.parser.repository.CityRepository;
import ru.itis.parser.repository.CountryRepository;
import ru.itis.parser.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public void upsertHotels(List<Hotel> hotels, Region region) {
        Country country = countryRepository
                .findByName(region.getCountry())
                .orElseGet(() -> countryRepository.save(new Country(null, region.getCountry())));
        City city = cityRepository
                .findByNameAndCountryId(region.getName(), country.getId())
                .orElseGet(() -> cityRepository.save(new City(null, region.getName(), country.getId(), region.getSlug())));
        hotels.forEach((hotel) -> hotel.setCityId(city.getId()));
        hotelRepository.upsert(hotels);
    }

    @Override
    public List<Hotel> findAllHotelsByFilters(FindAllHotelsParameters parameters) {
        List<City> cities;
        if (parameters.cityName() != null) {
            cities = cityRepository.findByName(parameters.cityName());
        } else {
            cities = cityRepository.findAll();
        }
        Predicate<City> cityPredicate = (city) -> true;
        if (parameters.countryName() != null) {
            Optional<Country> optCountry = countryRepository.findByName(parameters.countryName());
            if (optCountry.isEmpty()) {
                return new ArrayList<>();
            }
            Country country = optCountry.get();
            cityPredicate = (city) -> country.getId().equals(city.getCountryId());
        }
        Set<Long> cityIds = cities.stream().filter(cityPredicate).map(City::getId).collect(Collectors.toSet());
        return hotelRepository.findAllByFilters(parameters).stream()
                .filter((hotel) -> cityIds.contains(hotel.getCityId()))
                .toList();
    }
}
