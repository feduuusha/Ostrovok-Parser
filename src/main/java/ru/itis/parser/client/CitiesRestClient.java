package ru.itis.parser.client;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;
import ru.itis.parser.model.MultiComplete;

@Path("/api/site")
@RegisterRestClient(configKey = "cities-rest-client")
@ClientQueryParam(name = "locale", value = "${cities-rest-client.locale}")
public interface CitiesRestClient {

    @GET
    @Path("multicomplete.json")
    @Produces(MediaType.APPLICATION_JSON)
    MultiComplete searchCities(@RestQuery String query);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() >= 500) {
            return new ServerErrorException("The cities client responded with server error", response.getStatus());
        }
        if (response.getStatus() == 404) {
            return new NotFoundException("The cities client responded with HTTP 404");
        }
        if (response.getStatus() == 400) {
            return new NotFoundException("The cities client responded with HTTP 400");
        }
        if (response.getStatus() >= 400) {
            return new ClientErrorException("The cities client responded with client error", response.getStatus());
        }
        return null;
    }
}
