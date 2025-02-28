package ru.itis.parser.controller;

import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestQuery;
import ru.itis.parser.service.ParserService;

@Path("/api/v1/parse")
@RequiredArgsConstructor
public class ParserRestController {

    private final ParserService parserService;

    @POST
    @Consumes
    @Produces
    @Path("hotels")
    public void parseHotelsByCity(@RestQuery String city) {
        if (city == null || city.isBlank()) {
            throw new BadRequestException("City is required parameter");
        }
        parserService.parseHotelsByCity(city);
    }
}
