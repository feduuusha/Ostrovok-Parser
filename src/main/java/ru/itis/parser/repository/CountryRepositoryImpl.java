package ru.itis.parser.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import ru.itis.parser.entity.Country;
import ru.itis.parser.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class CountryRepositoryImpl implements CountryRepository {

    // language=sql
    private static final String SELECT_BY_NAME = "SELECT id, name FROM country WHERE name = ?";
    // language=sql
    private static final String INSERT_INTO = "INSERT INTO country (name) VALUES (?) RETURNING id";

    private final DataSource dataSource;
    private final RowMapper<Country> rowMapper;

    @Override
    public Optional<Country> findByName(String name) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_NAME)
        ) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public Country save(Country country) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO)
        ) {
            preparedStatement.setString(1, country.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            country.setId(resultSet.getLong("id"));
            return country;
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
