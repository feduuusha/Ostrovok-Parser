package ru.itis.parser.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import ru.itis.parser.entity.Hotel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HotelRowMapper implements RowMapper<Hotel> {

    @Override
    public Optional<Hotel> mapRow(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return Optional.of(Hotel.builder()
                    .id(resultSet.getLong("id"))
                    .ostrovokId(resultSet.getLong("ostrovok_id"))
                    .name(resultSet.getString("name"))
                    .address(resultSet.getString("address"))
                    .rating(resultSet.getDouble("rating"))
                    .countOfStars(resultSet.getInt("count_of_stars"))
                    .href(resultSet.getString("href"))
                    .minPricePerNight(resultSet.getInt("min_price_per_night"))
                    .cityId(resultSet.getLong("city_id"))
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public List<Hotel> mapRows(ResultSet resultSet) throws SQLException {
        List<Hotel> hotels = new ArrayList<>(100);
        Optional<Hotel> optional = mapRow(resultSet);
        while (optional.isPresent()) {
            hotels.add(optional.get());
            optional = mapRow(resultSet);
        }
        return hotels;
    }
}
