package ru.itis.parser.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import ru.itis.parser.controller.beanparameter.FindAllHotelsParameters;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.service.HotelService;

import java.util.List;

@Path("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelRestController {

    private final HotelService hotelService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Hotel> findAllHotelsByFilters(@BeanParam FindAllHotelsParameters parameters) {
        return hotelService.findAllHotelsByFilters(parameters);
    }
}
