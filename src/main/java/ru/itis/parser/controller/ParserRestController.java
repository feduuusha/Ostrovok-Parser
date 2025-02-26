package ru.itis.parser.controller;

import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestQuery;
import ru.itis.parser.service.ParserService;

@Path("/api/v1/parser")
@RequiredArgsConstructor
public class ParserRestController {

    private final ParserService parserService;

    @POST
    @Path("parse")
    @Consumes
    @Produces
    public void parseHotelsByCity(@RestQuery String city) {
        parserService.parseHotelsByCity(city);
    }
}
