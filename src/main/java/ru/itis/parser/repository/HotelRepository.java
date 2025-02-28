package ru.itis.parser.repository;

import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;

import java.util.List;

public interface HotelRepository {
    void upsert(List<Hotel> hotels);
    List<Hotel> findAllByFilters(FindAllHotelsParameters parameters);
}
