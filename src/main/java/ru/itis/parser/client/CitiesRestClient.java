package ru.itis.parser.client;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
}
