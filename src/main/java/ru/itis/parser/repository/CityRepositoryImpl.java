package ru.itis.parser.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import ru.itis.parser.entity.City;
import ru.itis.parser.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class CityRepositoryImpl implements CityRepository {

    //language=sql
    private static final String SELECT_BY_NAME_AND_COUNTRY_ID = "SELECT id, name, slug, country_id FROM city WHERE name = ? AND country_id = ?";
    // language=sql
    private static final String SELECT_BY_NAME = "SELECT id, name, slug, country_id FROM city WHERE LOWER(name) = LOWER(?)";
    // language=sql
    private static final String SELECT_ALL = "SELECT id, name, slug, country_id FROM city";
    //language=sql
    private static final String INSERT_INTO = "INSERT INTO city (name, slug, country_id) VALUES (?, ?, ?) RETURNING id";

    private final DataSource dataSource;
    private final RowMapper<City> rowMapper;
    @Override
    public Optional<City> findByNameAndCountryId(String name, Long id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_NAME_AND_COUNTRY_ID)
                ) {
            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public List<City> findByName(String name) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_NAME)
        ) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            return rowMapper.mapRows(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public City save(City city) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO)
        ) {
            preparedStatement.setString(1, city.getName());
            preparedStatement.setString(2, city.getSlug());
            preparedStatement.setLong(3, city.getCountryId());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            city.setId(resultSet.getLong("id"));
            return city;
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public List<City> findAll() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SELECT_ALL)
        ) {
            return rowMapper.mapRows(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
