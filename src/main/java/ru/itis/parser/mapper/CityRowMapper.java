package ru.itis.parser.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itis.parser.entity.City;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CityRowMapper implements RowMapper<City> {
    @Override
    public Optional<City> mapRow(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return Optional.of(City.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .slug(resultSet.getString("slug"))
                    .countryId(resultSet.getLong("country_id"))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public List<City> mapRows(ResultSet resultSet) throws SQLException {
        List<City> cities = new ArrayList<>(100);
        Optional<City> optional = mapRow(resultSet);
        while (optional.isPresent()) {
            cities.add(optional.get());
            optional = mapRow(resultSet);
        }
        return cities;
    }
}
