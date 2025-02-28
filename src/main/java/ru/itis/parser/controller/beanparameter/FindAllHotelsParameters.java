package ru.itis.parser.controller.beanparameter;

import jakarta.ws.rs.DefaultValue;
import org.jboss.resteasy.reactive.RestQuery;

public record FindAllHotelsParameters(
        @RestQuery String name,
        @DefaultValue(value = "0") @RestQuery("count-stars-from") Integer countOfStarsFrom,
        @DefaultValue(value = "5") @RestQuery("count-stars-to") Integer countOfStarsTo,
        @DefaultValue(value = "0") @RestQuery("min-price-from") Integer priceFrom,
        @DefaultValue(value = "2147483647") @RestQuery("min-price-to") Integer priceTo,
        @DefaultValue(value = "0.0") @RestQuery("rating-from") Double ratingFrom,
        @DefaultValue(value = "10.0") @RestQuery("rating-to") Double ratingTo,
        @RestQuery("city") String cityName,
        @RestQuery("country") String countryName
) {
}
