package ru.itis.parser.exception;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<String> mapException(BadRequestException ex) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException ex) {
        return RestResponse.status(Response.Status.NOT_FOUND, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(InternalServerErrorException ex) {
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
