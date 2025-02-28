package ru.itis.parser.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class HotelRepositoryImpl implements HotelRepository {

    // language=sql
    private static final String INSERT_INTO_ON_CONFLICT = """
                INSERT INTO hotel (ostrovok_id, name, address, count_of_stars, min_price_per_night, rating, href, city_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (ostrovok_id)
                DO UPDATE SET name = EXCLUDED.name, address = EXCLUDED.address, count_of_stars = EXCLUDED.count_of_stars,
                min_price_per_night = EXCLUDED.min_price_per_night, rating = EXCLUDED.rating, city_id = EXCLUDED.city_id
                """;
    //language=sql
    private static final String SELECT_BY_FILTERS = """
        SELECT h.id, h.ostrovok_id, h.name, h.address, h.count_of_stars, h.min_price_per_night, h.rating, h.href, h.city_id
        FROM hotel h
        WHERE h.count_of_stars BETWEEN ? AND ? AND h.min_price_per_night BETWEEN ? AND ? AND rating BETWEEN ? AND ?""";

    private final DataSource dataSource;
    private final RowMapper<Hotel> rowMapper;

    @Override
    public void upsert(List<Hotel> hotels) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_ON_CONFLICT)
        ) {
            for (Hotel hotel : hotels) {
                preparedStatement.setLong(1, hotel.getOstrovokId());
                preparedStatement.setString(2, hotel.getName());
                preparedStatement.setString(3, hotel.getAddress());
                preparedStatement.setInt(4, hotel.getCountOfStars());
                preparedStatement.setInt(5, hotel.getMinPricePerNight());
                if (hotel.getRating() == null) {
                    preparedStatement.setNull(6, 2);
                } else {
                    preparedStatement.setDouble(6, hotel.getRating());
                }
                preparedStatement.setString(7, hotel.getHref());
                preparedStatement.setLong(8, hotel.getCityId());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public List<Hotel> findAllByFilters(FindAllHotelsParameters parameters) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_FILTERS)
        ) {
            preparedStatement.setInt(1, parameters.countOfStarsFrom());
            preparedStatement.setInt(2, parameters.countOfStarsTo());
            preparedStatement.setInt(3, parameters.priceFrom());
            preparedStatement.setInt(4, parameters.priceTo());
            preparedStatement.setDouble(5, parameters.ratingFrom());
            preparedStatement.setDouble(6, parameters.ratingTo());
            ResultSet resultSet = preparedStatement.executeQuery();
            return rowMapper.mapRows(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
