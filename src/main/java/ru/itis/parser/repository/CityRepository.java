package ru.itis.parser.repository;

import ru.itis.parser.entity.City;

import java.util.List;
import java.util.Optional;

public interface CityRepository {
    Optional<City> findByNameAndCountryId(String name, Long id);
    List<City> findByName(String name);
    City save(City city);

    List<City> findAll();
}
