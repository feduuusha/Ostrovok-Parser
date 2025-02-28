package ru.itis.parser.service;

import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.Region;

import java.util.List;

public interface HotelService {
    List<Hotel> findAllHotelsByFilters(FindAllHotelsParameters parameters);
    void upsertHotels(List<Hotel> hotels, Region region);
}
