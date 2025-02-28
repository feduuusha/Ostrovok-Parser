package ru.itis.parser.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itis.parser.entity.Country;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CountryRowMapper implements RowMapper<Country> {
    @Override
    public Optional<Country> mapRow(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return Optional.of(Country.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public List<Country> mapRows(ResultSet resultSet) throws SQLException {
        List<Country> countries = new ArrayList<>(100);
        Optional<Country> optional = mapRow(resultSet);
        while (optional.isPresent()) {
            countries.add(optional.get());
            optional = mapRow(resultSet);
        }
        return countries;
    }
}
