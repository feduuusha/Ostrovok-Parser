package ru.itis.parser.repository;

import ru.itis.parser.entity.Country;

import java.util.Optional;

public interface CountryRepository {
    Optional<Country> findByName(String name);

    Country save(Country country);
}
